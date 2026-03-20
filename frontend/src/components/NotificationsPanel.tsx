import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { followApi, notificationApi } from '../api'
import type { NotificationDtoApi } from '../api'

interface NotificationsPanelProps {
  isOpen: boolean
  onClose: () => void
  onUpdatedUnread?: () => void
}

export function NotificationsPanel({ isOpen, onClose, onUpdatedUnread }: NotificationsPanelProps) {
  const [notifications, setNotifications] = useState<NotificationDtoApi[]>([])
  const [page, setPage] = useState(0)
  const [hasMore, setHasMore] = useState(true)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [feedbackMessage, setFeedbackMessage] = useState<{ type: 'accept' | 'reject'; username: string } | null>(null)

  const navigate = useNavigate()

  useEffect(() => {
    if (!feedbackMessage) return
    const t = setTimeout(() => setFeedbackMessage(null), 4000)
    return () => clearTimeout(t)
  }, [feedbackMessage])

  useEffect(() => {
    if (!isOpen) {
      setFeedbackMessage(null)
      setError(null)
      return
    }
    // Clear any old “accept/reject” message when reopening.
    setFeedbackMessage(null)
    setError(null)
    void loadPage(0, true)
  }, [isOpen])

  const loadPage = async (targetPage: number, replace = false) => {
    if (loading) return
    setLoading(true)
    setError(null)
    try {
      const { data } = await notificationApi.list(targetPage, 20)
      // Once a follow request is accepted/rejected we mark it read — don’t show it again.
      const cleaned = data.content.filter((n) => !(n.type === 'FOLLOW_REQUEST' && n.read))
      setNotifications((prev) => (replace ? cleaned : [...prev, ...cleaned]))
      setPage(targetPage)
      setHasMore(!data.last)
    } catch {
      setError('Failed to load notifications')
    } finally {
      setLoading(false)
    }
  }

  const handleOpenPost = async (n: NotificationDtoApi) => {
    if (!n.postId) return
    try {
      await notificationApi.markRead(n.id)
      setNotifications((prev) => prev.filter((item) => item.id !== n.id))
      onUpdatedUnread?.()
    } catch {
      // ignore for now
    }
    onClose()
    navigate(`/posts/${n.postId}`)
  }

  const handleOpenUser = async (n: NotificationDtoApi) => {
    if (!n.actorId) return
    try {
      await notificationApi.markRead(n.id)
      setNotifications((prev) => prev.filter((item) => item.id !== n.id))
      onUpdatedUnread?.()
    } catch {
      // ignore
    }
    onClose()
    navigate(`/users/${n.actorId}`)
  }

  const handleAcceptRequest = async (n: NotificationDtoApi) => {
    if (!n.actorId) return
    const username = n.actorUsername ?? 'User'
    try {
      await followApi.acceptRequest(n.actorId)
      await notificationApi.markRead(n.id)
      setNotifications((prev) => prev.filter((item) => item.id !== n.id))
      onUpdatedUnread?.()
      setFeedbackMessage({ type: 'accept', username })
      // Refresh so the new "FOLLOWED" notification appears.
      void loadPage(0, true)
    } catch {
      setError('Could not accept the request. Please try again.')
    }
  }

  const handleRejectRequest = async (n: NotificationDtoApi) => {
    if (!n.actorId) return
    const username = n.actorUsername ?? 'User'
    try {
      await followApi.rejectRequest(n.actorId)
      await notificationApi.markRead(n.id)
      setNotifications((prev) => prev.filter((item) => item.id !== n.id))
      onUpdatedUnread?.()
      setFeedbackMessage({ type: 'reject', username })
      // Refresh so the list is up to date after rejecting.
      void loadPage(0, true)
    } catch {
      setError('Could not decline the request. Please try again.')
    }
  }

  const handleFollowBack = async (n: NotificationDtoApi) => {
    if (!n.actorId) return
    try {
      await followApi.follow(n.actorId)
      await notificationApi.markRead(n.id)
      setNotifications((prev) => prev.filter((item) => item.id !== n.id))
      onUpdatedUnread?.()
    } catch {
      // optional: show error
    }
  }

  if (!isOpen) return null

  return (
    <div className="modal-overlay" onClick={onClose} role="presentation">
      <div
        className="modal-box notifications-modal"
        onClick={(e) => e.stopPropagation()}
        role="dialog"
        aria-label="Notifications"
      >
        <h3>Notifications</h3>
        {feedbackMessage && (
          <div
            className={`notification-feedback notification-feedback-${feedbackMessage.type}`}
            role="status"
          >
            {feedbackMessage.type === 'accept' ? (
              <span><strong>{feedbackMessage.username}</strong> is now following you.</span>
            ) : (
              <span>You declined the follow request from <strong>{feedbackMessage.username}</strong>.</span>
            )}
          </div>
        )}
        {error && (
          <div className="form-error" style={{ marginBottom: '0.5rem' }}>
            {error}
          </div>
        )}
        {notifications.length === 0 && !loading && (
          <div className="muted">No notifications yet.</div>
        )}

        <ul className="notification-list">
          {notifications.map((n) => (
            <li
              key={n.id}
              className={`notification-item ${n.read ? 'notification-read' : 'notification-unread'}`}
            >
              <div className="notification-main">
                <div className="notification-avatar">
                  {n.actorProfilePicture ? (
                    <img src={n.actorProfilePicture} alt="" className="avatar" />
                  ) : (
                    <div className="avatar avatar-placeholder" />
                  )}
                </div>
                <div className="notification-text">
                  {renderNotificationText(n, {
                    openPost: () => handleOpenPost(n),
                    openUser: () => handleOpenUser(n),
                    accept: () => handleAcceptRequest(n),
                    reject: () => handleRejectRequest(n),
                    followBack: () => handleFollowBack(n),
                  })}
                  <div className="notification-meta">
                    <span>{new Date(n.createdAt).toLocaleString()}</span>
                  </div>
                </div>
              </div>
            </li>
          ))}
        </ul>

        {hasMore && (
          <button
            type="button"
            className="btn btn-secondary"
            style={{ marginTop: '0.75rem', width: '100%' }}
            onClick={() => loadPage(page + 1)}
            disabled={loading}
          >
            {loading ? 'Loading…' : 'Load more'}
          </button>
        )}

        <button
          type="button"
          className="btn btn-secondary modal-close"
          style={{ marginTop: '0.75rem' }}
          onClick={onClose}
        >
          Close
        </button>
      </div>
    </div>
  )
}

interface RenderHelpers {
  openPost: () => void
  openUser: () => void
  accept: () => void
  reject: () => void
  followBack: () => void
}

function renderNotificationText(n: NotificationDtoApi, h: RenderHelpers) {
  switch (n.type) {
    case 'LIKE':
      return (
        <>
          <div>
            <button type="button" className="link-button" onClick={h.openUser}>
              {n.actorUsername}
            </button>{' '}
            liked your post{' '}
            <button type="button" className="link-button" onClick={h.openPost}>
              {n.postTitle || 'View post'}
            </button>
            .
          </div>
        </>
      )
    case 'COMMENT':
      return (
        <>
          <div>
            <button type="button" className="link-button" onClick={h.openUser}>
              {n.actorUsername}
            </button>{' '}
            commented on your post{' '}
            <button type="button" className="link-button" onClick={h.openPost}>
              {n.postTitle || 'View post'}
            </button>
            .
          </div>
          {n.commentText && <div className="notification-comment">“{n.commentText}”</div>}
        </>
      )
    case 'FOLLOW_REQUEST':
      return (
        <>
          <div>
            <button type="button" className="link-button" onClick={h.openUser}>
              {n.actorUsername}
            </button>{' '}
            requested to follow you.
          </div>
          <div className="notification-actions">
            <button type="button" className="btn btn-primary" onClick={h.accept}>
              Accept
            </button>
            <button type="button" className="btn btn-secondary" onClick={h.reject}>
              Reject
            </button>
          </div>
        </>
      )
    case 'FOLLOWED':
      return (
        <>
          <div>
            <button type="button" className="link-button" onClick={h.openUser}>
              {n.actorUsername}
            </button>{' '}
            started following you.
          </div>
          {n.canFollowBack && (
            <div className="notification-actions">
              <button type="button" className="btn btn-primary" onClick={h.followBack}>
                Follow back
              </button>
            </div>
          )}
        </>
      )
    case 'FOLLOW_REQUEST_ACCEPTED':
      return (
        <>
          <div>
            <button type="button" className="link-button" onClick={h.openUser}>
              {n.actorUsername}
            </button>{' '}
            accepted your follow request.
          </div>
        </>
      )
    case 'COMMENT_REPLY':
      return (
        <>
          <div>
            <button type="button" className="link-button" onClick={h.openUser}>
              {n.actorUsername}
            </button>{' '}
            replied to your comment on{' '}
            <button type="button" className="link-button" onClick={h.openPost}>
              {n.postTitle || 'View post'}
            </button>
            .
          </div>
          {n.commentText && <div className="notification-comment">“{n.commentText}”</div>}
        </>
      )
    case 'COMMENT_LIKE':
      return (
        <>
          <div>
            <button type="button" className="link-button" onClick={h.openUser}>
              {n.actorUsername}
            </button>{' '}
            liked your comment on{' '}
            <button type="button" className="link-button" onClick={h.openPost}>
              {n.postTitle || 'View post'}
            </button>
            .
          </div>
          {n.commentText && <div className="notification-comment">“{n.commentText}”</div>}
        </>
      )
    default:
      return <div>Unknown notification</div>
  }
}

