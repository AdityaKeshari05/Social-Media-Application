import { useEffect, useMemo, useRef, useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { messageApi, profileApi, searchApi } from '../api'
import type { ConversationDtoApi, MessageDtoApi, ProfileDto, UserPreview } from '../api'
import { useMessaging } from '../MessagingContext'

function useQuery() {
  const { search } = useLocation()
  return useMemo(() => new URLSearchParams(search), [search])
}

function formatTime(ts?: string | null) {
  if (!ts) return ''
  const d = new Date(ts)
  return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
}

export function MessagesPage() {
  const query = useQuery()
  const navigate = useNavigate()

  const { connected, lastMessage, lastReadReceipt, sendMessageWs, sendReadWs } = useMessaging()

  const [conversations, setConversations] = useState<ConversationDtoApi[]>([])
  const [loadingConvos, setLoadingConvos] = useState(true)
  const [convoError, setConvoError] = useState<string | null>(null)

  const [activeConversationId, setActiveConversationId] = useState<number | null>(null)
  const [messages, setMessages] = useState<MessageDtoApi[]>([])
  const [loadingMsgs, setLoadingMsgs] = useState(false)
  const [msgError, setMsgError] = useState<string | null>(null)

  const [draft, setDraft] = useState('')

  const [searchQuery, setSearchQuery] = useState('')
  const [searchResults, setSearchResults] = useState<UserPreview[]>([])
  const [searchLoading, setSearchLoading] = useState(false)

  const [canSendToUser, setCanSendToUser] = useState<boolean | null>(null)
  const [sendBlockedReason, setSendBlockedReason] = useState<string | null>(null)
  const [targetProfile, setTargetProfile] = useState<ProfileDto | null>(null)

  const autoSelectedForUserIdRef = useRef<number | null>(null)
  const markedReadForConversationRef = useRef<number | null>(null)

  const scrollRef = useRef<HTMLDivElement>(null)

  const userIdParam = query.get('userId')
  const convoIdParam = query.get('conversationId')

  useEffect(() => {
    autoSelectedForUserIdRef.current = null
  }, [userIdParam])

  useEffect(() => {
    if (!activeConversationId) return
    if (markedReadForConversationRef.current === activeConversationId) return
    markedReadForConversationRef.current = activeConversationId

    setConversations((prev) => prev.map((c) => (c.id === activeConversationId ? { ...c, unreadCount: 0 } : c)))
    void messageApi.markConversationRead(activeConversationId)
    sendReadWs(activeConversationId)
    window.dispatchEvent(new Event('goConnect:messages:read'))
  }, [activeConversationId, sendReadWs])

  const activeConversation = useMemo(
    () => conversations.find((c) => c.id === activeConversationId) ?? null,
    [conversations, activeConversationId],
  )

  // Load conversations
  useEffect(() => {
    let active = true
    ;(async () => {
      setLoadingConvos(true)
      setConvoError(null)
      try {
        const { data } = await messageApi.listConversations(0, 100)
        if (!active) return
        setConversations(data.content)
      } catch (e) {
        console.error(e)
        if (!active) return
        setConvoError('Failed to load conversations')
      } finally {
        if (active) setLoadingConvos(false)
      }
    })()
    return () => {
      active = false
    }
  }, [])

  // Search users within messages inbox
  useEffect(() => {
    if (!searchQuery.trim()) {
      setSearchResults([])
      return
    }
    const q = searchQuery.trim()
    const timeout = setTimeout(async () => {
      setSearchLoading(true)
      try {
        const { data } = await searchApi.searchUsers(q)
        setSearchResults(data)
      } catch {
        setSearchResults([])
      } finally {
        setSearchLoading(false)
      }
    }, 300)
    return () => clearTimeout(timeout)
  }, [searchQuery])

  // Select initial conversation by query param
  useEffect(() => {
    if (conversations.length === 0) return

    if (convoIdParam) {
      const cid = Number(convoIdParam)
      if (Number.isFinite(cid) && cid > 0) {
        setActiveConversationId(cid)
        return
      }
    }

    if (userIdParam) {
      const uid = Number(userIdParam)
      if (Number.isFinite(uid) && uid > 0) {
        const existing = conversations.find((c) => c.otherUser?.id === uid)
        if (existing) {
          setActiveConversationId(existing.id)
        } else {
          // No convo yet; user will send first message to create one.
          setActiveConversationId(null)
        }
      }
    }
  }, [conversations, convoIdParam, userIdParam])

  // Determine if we can send to a direct userId (privacy + follow)
  useEffect(() => {
    if (!userIdParam) {
      setCanSendToUser(null)
      setSendBlockedReason(null)
      setTargetProfile(null)
      return
    }
    const uid = Number(userIdParam)
    if (!Number.isFinite(uid) || uid <= 0) {
      setCanSendToUser(null)
      setSendBlockedReason(null)
      return
    }
    // If we already have an active conversation, assume allowed.
    const existing = conversations.find((c) => c.otherUser?.id === uid)
    if (existing) {
      setCanSendToUser(true)
      setSendBlockedReason(null)
      return
    }
    let active = true
    ;(async () => {
      try {
        const { data } = await profileApi.byUserId(uid)
        if (!active) return
        const p = data as ProfileDto & { isPrivate?: boolean; isFollowing?: boolean }
        setTargetProfile(p)
        const isPrivate = p.isPrivate ?? (p as any).private ?? false
        const isFollowing = p.isFollowing ?? (p as any).following ?? false
        if (isPrivate && !isFollowing) {
          setCanSendToUser(false)
          setSendBlockedReason(
            'You cannot send messages to this private account unless you follow them. Follow to start a conversation.',
          )
        } else {
          setCanSendToUser(true)
          setSendBlockedReason(null)
        }
      } catch {
        if (!active) return
        setCanSendToUser(null)
        setSendBlockedReason(null)
      }
    })()
    return () => {
      active = false
    }
  }, [userIdParam, conversations])

  // Load messages when activeConversationId changes
  useEffect(() => {
    if (!activeConversationId) {
      setMessages([])
      return
    }
    let active = true
    ;(async () => {
      setLoadingMsgs(true)
      setMsgError(null)
      try {
        const { data } = await messageApi.listMessages(activeConversationId, 0, 200)
        if (!active) return
        setMessages(data.content)
        // mark read (REST + WS event)
        void messageApi.markConversationRead(activeConversationId)
        sendReadWs(activeConversationId)
      } catch (e) {
        console.error(e)
        if (!active) return
        setMsgError('Failed to load messages')
      } finally {
        if (active) setLoadingMsgs(false)
      }
    })()
    return () => {
      active = false
    }
  }, [activeConversationId, sendReadWs])

  // Apply incoming WS message
  useEffect(() => {
    if (!lastMessage) return

    // If we are in "start chat" mode via userIdParam (no conversation selected yet),
    // selecting should happen as soon as the first WS message arrives.
    if (!activeConversationId && userIdParam) {
      const targetUid = Number(userIdParam)
      if (Number.isFinite(targetUid) && autoSelectedForUserIdRef.current !== targetUid) {
        autoSelectedForUserIdRef.current = targetUid
        void (async () => {
          try {
            const { data } = await messageApi.listConversations(0, 100)
            setConversations(data.content)
            const found =
              data.content.find((c) => c.otherUser?.id === targetUid) ??
              data.content.find((c) => c.id === lastMessage.conversationId) ??
              null
            if (found) {
              setActiveConversationId(found.id)
              navigate(`/messages?conversationId=${found.id}`)
            }
          } catch {
            // ignore; normal message rendering will continue
          }
        })()
      }
    }

    // Update conversation preview + unread counters
    setConversations((prev) =>
      prev.map((c) =>
        c.id === lastMessage.conversationId
          ? {
              ...c,
              lastMessage: lastMessage.content,
              lastMessageAt: lastMessage.createdAt,
              unreadCount:
                c.id === activeConversationId
                  ? 0
                  : c.otherUser?.username === lastMessage.senderUsername
                    ? Math.min(999, (c.unreadCount ?? 0) + 1)
                    : c.unreadCount ?? 0,
            }
          : c,
      ),
    )

    if (lastMessage.conversationId === activeConversationId) {
      setMessages((prev) => {
        if (prev.some((m) => m.id === lastMessage.id)) return prev
        return [...prev, lastMessage]
      })
      // keep read state fresh
      sendReadWs(activeConversationId)
      void messageApi.markConversationRead(activeConversationId)
      requestAnimationFrame(() => {
        // Keep view anchored at the latest message (no "jumping" upwards).
        scrollRef.current?.scrollTo({ top: scrollRef.current.scrollHeight, behavior: 'auto' })
      })
    }
  }, [lastMessage, activeConversationId, sendReadWs])

  // Apply incoming read receipt event (simple: we just refresh local messages readAt by refetching if active)
  useEffect(() => {
    if (!lastReadReceipt) return
    if (lastReadReceipt.conversationId !== activeConversationId) return
    // Minimal: refetch messages for accuracy
    void (async () => {
      try {
        const { data } = await messageApi.listMessages(lastReadReceipt.conversationId, 0, 200)
        setMessages(data.content)
      } catch {
        // ignore
      }
    })()
  }, [lastReadReceipt, activeConversationId])

  // Always keep view anchored to the latest message for the active chat
  useEffect(() => {
    if (!activeConversationId) return
    if (!messages.length) return
    requestAnimationFrame(() => {
      scrollRef.current?.scrollTo({ top: scrollRef.current.scrollHeight, behavior: 'auto' })
    })
  }, [messages, activeConversationId])

  function openConversation(convo: ConversationDtoApi) {
    const dispatchReadEvent = () => window.dispatchEvent(new Event('goConnect:messages:read'))

    // Double-press: unselect + show empty state.
    if (activeConversationId === convo.id) {
      navigate('/messages')
      setActiveConversationId(null)
      dispatchReadEvent()
      return
    }

    // Single press: select and clear unread instantly.
    setConversations((prev) => prev.map((c) => (c.id === convo.id ? { ...c, unreadCount: 0 } : c)))
    setActiveConversationId(convo.id)

    // Mark as read immediately (don't wait for listMessages fetch).
    void messageApi.markConversationRead(convo.id)
    sendReadWs(convo.id)
    dispatchReadEvent()

    navigate(`/messages?conversationId=${convo.id}`)
  }

  async function send() {
    const text = draft.trim()
    if (!text) return

    const byUser = userIdParam ? Number(userIdParam) : null

    // Determine recipientId
    let recipientId: number | null = activeConversation?.otherUser?.id ?? null
    if (!recipientId && byUser && Number.isFinite(byUser) && byUser > 0) recipientId = byUser

    if (!recipientId) {
      setMsgError('Select a chat or open a user to message.')
      return
    }

    setDraft('')
    setMsgError(null)

    // Prefer WS for instant echo/deliveredAt; fallback to REST if WS not connected.
    if (connected) {
      sendMessageWs(recipientId, text)
      return
    }

    try {
      const { data } = await messageApi.sendToUser(recipientId, text)
      // If this created a conversation, redirect to it.
      if (!activeConversationId || activeConversationId !== data.conversationId) {
        navigate(`/messages?conversationId=${data.conversationId}`)
        setActiveConversationId(data.conversationId)
      }
      setMessages((prev) => (prev.some((m) => m.id === data.id) ? prev : [...prev, data]))
      requestAnimationFrame(() => {
        scrollRef.current?.scrollTo({ top: scrollRef.current.scrollHeight, behavior: 'auto' })
      })
    } catch (e) {
      console.error(e)
      setMsgError('Failed to send message')
    }
  }

  const longPressTimerRef = useRef<number | null>(null)
  const longPressTriggeredRef = useRef(false)

  async function deleteConversation(convoId: number) {
    const ok = window.confirm('Delete chat? This conversation will be removed from your messages.')
    if (!ok) return
    try {
      await messageApi.deleteConversation(convoId)
      setConversations((prev) => prev.filter((c) => c.id !== convoId))
      if (activeConversationId === convoId) {
        setActiveConversationId(null)
        navigate('/messages')
      }
      window.dispatchEvent(new Event('goConnect:messages:read'))
    } catch (e) {
      console.error(e)
      setMsgError('Failed to delete chat')
    }
  }

  function startLongPress(convoId: number) {
    longPressTriggeredRef.current = false
    if (longPressTimerRef.current) window.clearTimeout(longPressTimerRef.current)
    longPressTimerRef.current = window.setTimeout(() => {
      longPressTriggeredRef.current = true
      void deleteConversation(convoId)
    }, 650)
  }

  function cancelLongPress() {
    if (longPressTimerRef.current) window.clearTimeout(longPressTimerRef.current)
    longPressTimerRef.current = null
  }

  const canSend =
    Boolean(activeConversationId && activeConversation) ||
    (!activeConversationId && userIdParam && (canSendToUser === null || canSendToUser === true))

  return (
    <div className="messages-page">
      <div className="messages-inbox">
        <div className="messages-inbox-header">
          <h1 className="page-title" style={{ margin: 0 }}>
            Messages
          </h1>
          <div className="muted" style={{ fontSize: 12 }}>
            <span
              className={`status-dot ${connected ? 'status-dot-online' : 'status-dot-offline'}`}
              style={{ marginRight: 4 }}
            />
            {connected ? 'Online' : 'Offline'}
          </div>
        </div>

        <div className="messages-inbox-search">
          <input
            type="search"
            placeholder="Search people to message…"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
          />
        </div>

        {searchQuery.trim() && (
          <div className="messages-search-results">
            {searchLoading && <div className="muted">Searching…</div>}
            {!searchLoading && searchResults.length === 0 && (
              <div className="muted">No users found for &quot;{searchQuery.trim()}&quot;</div>
            )}
            {!searchLoading && searchResults.length > 0 && (
              <ul className="inbox-list">
                {searchResults.map((u) => (
                  <li key={u.id}>
                    <button
                      type="button"
                      className="inbox-item"
                      onClick={() => {
                        setSearchQuery('')
                        const existing = conversations.find((c) => c.otherUser?.id === u.id)
                        if (existing) {
                          openConversation(existing)
                        } else {
                          navigate(`/messages?userId=${u.id}`)
                          setActiveConversationId(null)
                        }
                      }}
                    >
                      {u.profilePicture ? (
                        <img src={u.profilePicture} className="avatar" alt="" />
                      ) : (
                        <div className="avatar avatar-placeholder" />
                      )}
                      <div className="inbox-main">
                        <div className="inbox-top">
                          <div className="inbox-username">{u.username}</div>
                        </div>
                        <div className="inbox-preview">
                          {u.following ? 'You follow this user' : 'Tap to open a new chat'}
                        </div>
                      </div>
                    </button>
                  </li>
                ))}
              </ul>
            )}
          </div>
        )}

        {!searchQuery.trim() && (
          <>
            {convoError && <div className="form-error">{convoError}</div>}
            {loadingConvos ? (
              <div className="muted">Loading chats…</div>
            ) : conversations.length === 0 ? (
              <div className="muted">No chats yet.</div>
            ) : (
              <ul className="inbox-list">
                {conversations.map((c) => (
                  <li key={c.id}>
                    <button
                      type="button"
                      className={`inbox-item ${c.id === activeConversationId ? 'inbox-item-active' : ''}`}
                      onClick={() => {
                        if (longPressTriggeredRef.current) {
                          longPressTriggeredRef.current = false
                          return
                        }
                        openConversation(c)
                      }}
                      onContextMenu={(e) => {
                        e.preventDefault()
                        void deleteConversation(c.id)
                      }}
                      onPointerDown={() => startLongPress(c.id)}
                      onPointerUp={cancelLongPress}
                      onPointerLeave={cancelLongPress}
                    >
                      {c.otherUser?.profilePicture ? (
                        <img src={c.otherUser.profilePicture} className="avatar" alt="" />
                      ) : (
                        <div className="avatar avatar-placeholder" />
                      )}
                      <div className="inbox-main">
                        <div className="inbox-top">
                          <div className="inbox-username">{c.otherUser?.username ?? 'User'}</div>
                          <div className="inbox-time">{formatTime(c.lastMessageAt)}</div>
                        </div>
                        <div className="inbox-preview">{c.lastMessage ?? 'No messages yet'}</div>
                      </div>
                      {c.unreadCount > 0 && (
                        <div className="inbox-badge">{c.unreadCount >= 4 ? '4+' : `${c.unreadCount}+`}</div>
                      )}
                    </button>
                  </li>
                ))}
              </ul>
            )}
          </>
        )}
      </div>

      <div className="messages-chat">
        {!activeConversation && !userIdParam ? (
          <div className="chat-empty">
            <div className="go-connect-logo">
              Go-<span>Connect</span>
            </div>
            <h2 className="chat-empty-title">Stay close to your people</h2>
            <p className="chat-empty-subtitle">
              Choose a conversation on the left or search for someone new. Go-Connect Messages keep your social circle
              close and every chat just one tap away.
            </p>
          </div>
        ) : activeConversation ? (
          <>
            <div className="chat-header">
              <div className="chat-title">
                {activeConversation.otherUser?.profilePicture ? (
                  <img src={activeConversation.otherUser.profilePicture} className="avatar" alt="" />
                ) : (
                  <div className="avatar avatar-placeholder" />
                )}
                <div style={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                  <div className="chat-username">{activeConversation.otherUser?.username}</div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 6, fontSize: 12, color: 'var(--text-muted)' }}>
                    <span className={`status-dot ${connected ? 'status-dot-online' : 'status-dot-offline'}`} />
                    <span>
                      {connected
                        ? 'Active now'
                        : activeConversation.lastMessageAt
                        ? `Last active at ${formatTime(activeConversation.lastMessageAt)}`
                        : 'Last active recently'}
                    </span>
                    {activeConversation.otherUser?.id && (
                      <>
                        <span>&middot;</span>
                        <Link className="chat-profile-link" to={`/users/${activeConversation.otherUser.id}`}>
                          View profile
                        </Link>
                      </>
                    )}
                  </div>
                </div>
              </div>
            </div>

            <div className="chat-body" ref={scrollRef}>
              {msgError && <div className="form-error">{msgError}</div>}
              {loadingMsgs ? (
                <div className="muted">Loading messages…</div>
              ) : messages.length === 0 ? (
                <div className="muted">No messages yet.</div>
              ) : (
                <div className="chat-messages">
                  {messages.map((m) => {
                    const isMine = activeConversation.otherUser?.username !== m.senderUsername
                    return (
                      <div key={m.id} className={`bubble-row ${isMine ? 'bubble-row-me' : 'bubble-row-them'}`}>
                        <div className={`bubble ${isMine ? 'bubble-me' : 'bubble-them'}`}>
                          <div>{m.content}</div>
                          <div className="bubble-meta">
                            <span>{formatTime(m.createdAt)}</span>
                            {isMine && (
                              <span className="bubble-status">
                                {m.readAt ? 'Seen' : m.deliveredAt ? 'Delivered' : 'Sent'}
                              </span>
                            )}
                          </div>
                        </div>
                      </div>
                    )
                  })}
                </div>
              )}
            </div>

            {canSend ? (
              <form
                className="chat-compose"
                onSubmit={(e) => {
                  e.preventDefault()
                  void send()
                }}
              >
                <input value={draft} onChange={(e) => setDraft(e.target.value)} placeholder="Message…" />
                <button type="submit" className="btn btn-primary" disabled={!draft.trim()}>
                  Send
                </button>
              </form>
            ) : (
              <div className="chat-compose chat-compose-disabled">
                <p className="muted">
                  {sendBlockedReason ??
                    'You cannot send messages to this user right now. Follow them or wait until messaging is available.'}
                </p>
              </div>
            )}
          </>
        ) : userIdParam ? (
          <div className="chat-start-wrapper">
            <div className="chat-header">
              <div className="chat-title">
                {targetProfile?.profilePicture ? (
                  <img src={targetProfile.profilePicture} className="avatar" alt={targetProfile.username} />
                ) : (
                  <div className="avatar avatar-placeholder" />
                )}
                <div style={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                  <div className="chat-username">{targetProfile?.username ?? 'User'}</div>
                  <div className="chat-start-line">
                    Start a private conversation with{' '}
                    <span className="chat-start-username">@{targetProfile?.username ?? 'user'}</span> and keep your
                    connections close on Go-Connect.
                  </div>
                </div>
              </div>
            </div>
            <div className="chat-body" ref={scrollRef}>
              {canSendToUser === false && sendBlockedReason ? (
                <div className="chat-empty">
                  <h2 style={{ marginTop: 0 }}>Messaging not available</h2>
                  <p className="muted">{sendBlockedReason}</p>
                </div>
              ) : (
                <div className="chat-empty">
                  <p className="chat-empty-subtitle">
                    Say hello and start building a meaningful conversation. Only you and {targetProfile?.username ?? 'this user'} can see this chat.
                  </p>
                </div>
              )}
            </div>
            {canSend && (
              <form
                className="chat-compose chat-compose-start"
                onSubmit={(e) => {
                  e.preventDefault()
                  void send()
                }}
              >
                <input value={draft} onChange={(e) => setDraft(e.target.value)} placeholder="Write your first message…" />
                <button type="submit" className="btn btn-primary" disabled={!draft.trim()}>
                  Send
                </button>
              </form>
            )}
          </div>
        ) : null}
      </div>
    </div>
  )
}

