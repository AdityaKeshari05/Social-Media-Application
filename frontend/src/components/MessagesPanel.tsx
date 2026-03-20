import { useEffect, useMemo, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { messageApi, type ConversationDtoApi, type MessageDtoApi } from '../api'
import { useMessaging } from '../MessagingContext'

interface MessagesPanelProps {
  isOpen: boolean
  onClose: () => void
  onOpenConversation: (conversationId: number) => void
}

type DrawerView = 'list' | 'chat'

function formatUnread(n: number) {
  if (n <= 0) return null
  if (n >= 4) return '4+'
  return String(n)
}

function formatPreview(lastMessage: string | null, unreadCount: number) {
  if (!lastMessage) return 'No messages yet'
  const text = lastMessage.trim()
  if (text.length <= 32) return text
  const max = unreadCount === 1 ? 40 : 28
  return text.slice(0, max).trimEnd() + '…'
}

function formatLastSeen(ts?: string | null) {
  if (!ts) return 'Last active recently'
  const d = new Date(ts)
  const now = new Date()
  const isToday = d.toDateString() === now.toDateString()
  if (isToday) {
    return `Last active today at ${d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}`
  }
  return `Last active on ${d.toLocaleDateString()}`
}

export function MessagesPanel({ isOpen, onClose }: MessagesPanelProps) {
  const [conversations, setConversations] = useState<ConversationDtoApi[]>([])
  const [loadingConvos, setLoadingConvos] = useState(false)
  const [convoError, setConvoError] = useState<string | null>(null)

  const [view, setView] = useState<DrawerView>('list')
  const [activeConversationId, setActiveConversationId] = useState<number | null>(null)
  const [messages, setMessages] = useState<MessageDtoApi[]>([])
  const [loadingMsgs, setLoadingMsgs] = useState(false)
  const [msgError, setMsgError] = useState<string | null>(null)
  const [draft, setDraft] = useState('')

  const scrollRef = useRef<HTMLDivElement | null>(null)

  const { connected, lastMessage, sendMessageWs, sendReadWs } = useMessaging()
  const navigate = useNavigate()

  const sorted = useMemo(
    () =>
      [...conversations].sort((a, b) => {
        const at = a.lastMessageAt ? new Date(a.lastMessageAt).getTime() : 0
        const bt = b.lastMessageAt ? new Date(b.lastMessageAt).getTime() : 0
        return bt - at
      }),
    [conversations],
  )

  const activeConversation = useMemo(
    () => conversations.find((c) => c.id === activeConversationId) ?? null,
    [conversations, activeConversationId],
  )

  useEffect(() => {
    if (!isOpen) return
    let active = true
    ;(async () => {
      setLoadingConvos(true)
      setConvoError(null)
      try {
        const { data } = await messageApi.listConversations(0, 50)
        if (!active) return
        setConversations(data.content)
      } catch (e) {
        console.error(e)
        if (!active) return
        setConvoError('Failed to load messages')
      } finally {
        if (active) setLoadingConvos(false)
      }
    })()
    return () => {
      active = false
    }
  }, [isOpen])

  useEffect(() => {
    if (!isOpen) {
      setView('list')
      setActiveConversationId(null)
      setMessages([])
      setDraft('')
    }
  }, [isOpen])

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
        const { data } = await messageApi.listMessages(activeConversationId, 0, 100)
        if (!active) return
        setMessages(data.content)
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
  }, [activeConversationId])

  useEffect(() => {
    if (!loadingMsgs && messages.length > 0 && scrollRef.current) {
      scrollRef.current.scrollTop = scrollRef.current.scrollHeight
    }
  }, [loadingMsgs, messages])

  // Apply incoming WS message for drawer view
  useEffect(() => {
    if (!lastMessage) return
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
      // Keep read state consistent in real-time.
      sendReadWs(activeConversationId)
      void messageApi.markConversationRead(activeConversationId)
      requestAnimationFrame(() => {
        scrollRef.current?.scrollTo({ top: scrollRef.current.scrollHeight, behavior: 'auto' })
      })
    }
  }, [lastMessage, activeConversationId])

  if (!isOpen) return null

  return (
    <div className="messages-floating" aria-label="Messages">
      <div className="messages-drawer">
        {view === 'list' && (
          <>
            <div className="messages-drawer-header">
              <h3 style={{ margin: 0 }}>Messages</h3>
              <div className="messages-drawer-header-right">
                <span className={`status-pill ${connected ? 'status-pill-online' : 'status-pill-offline'}`}>
                  {connected ? 'Online' : 'Offline'}
                </span>
                <button
                  type="button"
                  className="icon-button"
                  onClick={() => {
                    onClose()
                    navigate('/messages')
                  }}
                  aria-label="Open full messages view"
                  title="Open full messages view"
                >
                  ↔
                </button>
                <button type="button" className="icon-button" onClick={onClose} aria-label="Close messages">
                  ×
                </button>
              </div>
            </div>

            {convoError && <div className="form-error">{convoError}</div>}
            {loadingConvos && (
              <div className="muted" style={{ padding: '0.5rem 0' }}>
                Loading chats…
              </div>
            )}

            {!loadingConvos && sorted.length === 0 && (
              <div className="muted" style={{ padding: '0.5rem 0' }}>
                No chats yet.
              </div>
            )}

            <ul className="messages-list">
              {sorted.map((c) => {
                const badge = formatUnread(c.unreadCount)
                return (
                  <li key={c.id}>
                    <button
                      type="button"
                      className={`messages-item ${c.unreadCount > 0 ? 'messages-item-unread' : ''}`}
                      onClick={() => {
                        setActiveConversationId(c.id)
                        setView('chat')
                        // Clear unread instantly for this conversation.
                        setConversations((prev) => prev.map((x) => (x.id === c.id ? { ...x, unreadCount: 0 } : x)))
                        void messageApi.markConversationRead(c.id)
                        sendReadWs(c.id)
                        window.dispatchEvent(new Event('goConnect:messages:read'))
                      }}
                    >
                      <div className="messages-avatar">
                        {c.otherUser?.profilePicture ? (
                          <img src={c.otherUser.profilePicture} alt="" className="avatar" />
                        ) : (
                          <div className="avatar avatar-placeholder" />
                        )}
                      </div>
                      <div className="messages-main">
                        <div className="messages-top">
                          <div className="messages-username">{c.otherUser?.username ?? 'User'}</div>
                          {c.lastMessageAt ? (
                            <div className="messages-time">
                              {new Date(c.lastMessageAt).toLocaleTimeString([], {
                                hour: '2-digit',
                                minute: '2-digit',
                              })}
                            </div>
                          ) : null}
                        </div>
                        <div className="messages-preview">{formatPreview(c.lastMessage, c.unreadCount)}</div>
                      </div>
                      {badge ? <div className="messages-unread-pill">{badge}</div> : null}
                    </button>
                  </li>
                )
              })}
            </ul>
          </>
        )}

        {view === 'chat' && activeConversation && (
          <div className="drawer-chat">
            <div className="drawer-chat-header">
              <button
                type="button"
                className="icon-button back-button"
                onClick={() => {
                  setView('list')
                  setActiveConversationId(null)
                  setMessages([])
                  setDraft('')
                }}
                aria-label="Back to conversations"
              >
                ←
              </button>
              <div className="drawer-chat-title">
                <div className="drawer-chat-name-row">
                  <span className="drawer-chat-username">{activeConversation.otherUser?.username}</span>
                  <span className={`status-dot ${connected ? 'status-dot-online' : 'status-dot-offline'}`} />
                </div>
                <div className="drawer-chat-subtitle">
                  {connected ? 'Active now' : formatLastSeen(activeConversation.lastMessageAt)}
                </div>
              </div>
              <div className="drawer-chat-actions">
                <button type="button" className="icon-button" onClick={onClose} aria-label="Close messages">
                  ×
                </button>
              </div>
            </div>
            <div className="drawer-chat-body" ref={scrollRef}>
              {msgError && <div className="form-error">{msgError}</div>}
              {loadingMsgs ? (
                <div className="muted">Loading messages…</div>
              ) : messages.length === 0 ? (
                <div className="muted">No messages yet.</div>
              ) : (
                <div className="drawer-chat-messages">
                  {messages.map((m) => {
                    const isMine = activeConversation.otherUser?.username !== m.senderUsername
                    return (
                      <div key={m.id} className={`bubble-row ${isMine ? 'bubble-row-me' : 'bubble-row-them'}`}>
                        <div className={`bubble ${isMine ? 'bubble-me' : 'bubble-them'}`}>
                          <div>{m.content}</div>
                          <div className="bubble-meta">
                            <span>
                              {new Date(m.createdAt).toLocaleTimeString([], {
                                hour: '2-digit',
                                minute: '2-digit',
                              })}
                            </span>
                          </div>
                        </div>
                      </div>
                    )
                  })}
                </div>
              )}
            </div>
            <form
              className="drawer-chat-compose"
              onSubmit={(e) => {
                e.preventDefault()
                const text = draft.trim()
                if (!text) return
                setDraft('')
                setMsgError(null)
                if (!activeConversation.otherUser?.id) {
                  setMsgError('Cannot send message: unknown recipient.')
                  return
                }
                if (connected) {
                  // Real-time send: UI will update from WS echo.
                  sendMessageWs(activeConversation.otherUser.id, text)
                  return
                }
                void (async () => {
                  try {
                    const { data } = await messageApi.sendToUser(activeConversation.otherUser!.id, text)
                    setMessages((prev) => (prev.some((m) => m.id === data.id) ? prev : [...prev, data]))
                  } catch (err) {
                    console.error(err)
                    setMsgError('Failed to send message')
                  }
                })()
              }}
            >
              <input
                value={draft}
                onChange={(e) => setDraft(e.target.value)}
                placeholder="Message…"
                aria-label="Message"
              />
              <button type="submit" className="btn btn-primary" disabled={!draft.trim()}>
                Send
              </button>
            </form>
          </div>
        )}
      </div>
    </div>
  )
}

