import { useEffect, useState } from 'react'
import type { ReactNode } from 'react'
import { Link, NavLink, useNavigate, useLocation } from 'react-router-dom'
import { useAuth } from '../AuthContext'
import { messageApi, notificationApi } from '../api'
import { NotificationsPanel } from './NotificationsPanel'
import { MessagesPanel } from './MessagesPanel'
import { useMessaging } from '../MessagingContext'

interface LayoutProps {
  children: ReactNode
}

export function Layout({ children }: LayoutProps) {
  const { isAuthenticated, logout } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()

  const [showNotifications, setShowNotifications] = useState(false)
  const [showMessages, setShowMessages] = useState(false)
  const [showLogoutConfirm, setShowLogoutConfirm] = useState(false)
  const [unreadCount, setUnreadCount] = useState(0)
  const [messageUnreadCount, setMessageUnreadCount] = useState(0)

  const { lastMessage } = useMessaging()

  const refreshUnreadCount = async () => {
    if (!isAuthenticated) {
      setUnreadCount(0)
      return
    }
    try {
      const { data } = await notificationApi.unreadCount()
      setUnreadCount(data)
    } catch {
      // ignore
    }
  }

  useEffect(() => {
    void refreshUnreadCount()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isAuthenticated])

  const refreshMessageUnread = async () => {
    if (!isAuthenticated) {
      setMessageUnreadCount(0)
      return
    }
    try {
      const { data } = await messageApi.listConversations(0, 50)
      const total = data.content.reduce((sum, c) => sum + (c.unreadCount ?? 0), 0)
      setMessageUnreadCount(total)
    } catch {
      // ignore
    }
  }

  useEffect(() => {
    void refreshMessageUnread()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isAuthenticated])

  // Keep messages badge in sync with real-time incoming messages
  useEffect(() => {
    if (!lastMessage) return
    void refreshMessageUnread()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [lastMessage])

  useEffect(() => {
    // Let message pages/drawer force-refresh unread badge instantly after "read" actions.
    const handler = () => {
      void refreshMessageUnread()
    }
    window.addEventListener('goConnect:messages:read', handler)
    return () => window.removeEventListener('goConnect:messages:read', handler)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isAuthenticated])

  const handleOpenNotifications = async () => {
    setShowNotifications(true)
    await refreshUnreadCount()
  }

  const handleCloseNotifications = () => {
    setShowNotifications(false)
  }

  const handleOpenMessages = async () => {
    setShowMessages(true)
    await refreshMessageUnread()
  }

  const handleCloseMessages = () => {
    setShowMessages(false)
  }

  return (
    <div className="app-shell">
      <header className="app-header">
        <Link to="/" className="brand">
          Go-<span>Connect</span>
        </Link>
        <nav className="nav-links">
          <NavLink to="/" className={({ isActive }) => (isActive ? 'active' : '')}>
            Feed
          </NavLink>
          {isAuthenticated && (
            <>
              <NavLink to="/posts/new" className={({ isActive }) => (isActive ? 'active' : '')}>
                New Post
              </NavLink>
              <NavLink to="/me" className={({ isActive }) => (isActive ? 'active' : '')}>
                My Profile
              </NavLink>
            </>
          )}
        </nav>
        <div className="auth-actions">
          {isAuthenticated && (
            <Link to="/search" className="icon-button icon-button-search" aria-label="Search users">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <circle cx="11" cy="11" r="8" />
                <path d="m21 21-4.35-4.35" />
              </svg>
            </Link>
          )}
          {isAuthenticated ? (
            <>
              <button
                type="button"
                className="icon-button icon-button-bell"
                onClick={handleOpenNotifications}
                aria-label="Notifications"
              >
                <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
                  <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
                  <path d="M13.73 21a2 2 0 0 1-3.46 0" />
                </svg>
                {unreadCount > 0 && <span className="badge-pill">{unreadCount}</span>}
              </button>
              <button
                type="button"
                onClick={() => setShowLogoutConfirm(true)}
                className="btn btn-secondary"
              >
                Logout
              </button>
            </>
          ) : (
            <>
              <Link to="/login" className="btn btn-ghost">
                Log in
              </Link>
              <Link to="/register" className="btn btn-primary">
                Sign up
              </Link>
            </>
          )}
        </div>
      </header>
      <main className="app-main">
        {isAuthenticated && (
          <aside className="app-sidebar" aria-label="Primary">
            <Link to="/settings" className="app-sidebar-icon" aria-label="Settings" title="Settings">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <circle cx="12" cy="12" r="3" />
                <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z" />
              </svg>
              <span className="app-sidebar-label">Settings</span>
            </Link>
              <button
              type="button"
                className="app-sidebar-icon app-sidebar-icon-button message-sidebar-icon"
              aria-label="Messages"
              title="Messages"
              onClick={handleOpenMessages}
            >
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M21 15a4 4 0 0 1-4 4H7l-4 3V7a4 4 0 0 1 4-4h10a4 4 0 0 1 4 4z" />
              </svg>
              <span className="app-sidebar-label">Messages</span>
              {messageUnreadCount > 0 && (
                <span className="message-sidebar-badge">
                  {messageUnreadCount > 99 ? '99+' : messageUnreadCount}
                </span>
              )}
            </button>
          </aside>
        )}
        <div
          className={`app-content ${
            location.pathname === '/me' ||
            location.pathname.startsWith('/users/') ||
            location.pathname.startsWith('/messages')
              ? 'profile-layout'
              : ''
          }`}
        >
          {children}
        </div>
      </main>
      {showLogoutConfirm && (
        <div
          className="modal-overlay"
          onClick={() => setShowLogoutConfirm(false)}
          role="presentation"
        >
          <div
            className="modal-box"
            onClick={(e) => e.stopPropagation()}
            role="dialog"
            aria-label="Log out confirmation"
          >
            <h3>Log out?</h3>
            <p className="muted">Are you sure you want to log out of your account?</p>
            <div className="modal-actions">
              <button type="button" className="btn btn-secondary" onClick={() => setShowLogoutConfirm(false)}>
                Cancel
              </button>
              <button
                type="button"
                className="btn btn-primary btn-danger"
                onClick={() => {
                  logout()
                  navigate('/login')
                  setShowLogoutConfirm(false)
                }}
              >
                Log out
              </button>
            </div>
          </div>
        </div>
      )}
      <NotificationsPanel
        isOpen={showNotifications}
        onClose={handleCloseNotifications}
        onUpdatedUnread={refreshUnreadCount}
      />
      <MessagesPanel
        isOpen={showMessages}
        onClose={handleCloseMessages}
        onOpenConversation={(conversationId) => navigate(`/messages?conversationId=${conversationId}`)}
      />
    </div>
  )
}

