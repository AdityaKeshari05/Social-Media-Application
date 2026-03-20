import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { followApi } from '../api'
import type { UserPreview, UserPreviewWithFollowStatus } from '../api'

interface UserListModalProps {
  title: string
  users: (UserPreview | UserPreviewWithFollowStatus)[]
  onClose: () => void
  /** Which kind of list this represents. Null means no special actions. */
  kind?: 'followers' | 'following' | 'mutual' | null
  /** Only true if viewing our *own* profile's lists. */
  isOwnList?: boolean
  /** When viewing someone else's list, pass current user id so we hide Follow/Following for the current user's row. */
  currentUserId?: number | null
}

export function UserListModal({ title, users, onClose, kind = null, isOwnList = false, currentUserId = null }: UserListModalProps) {
  const [localUsers, setLocalUsers] = useState<(UserPreview & { following?: boolean })[]>(
    users.map((u) => ({ ...u, following: 'following' in u ? u.following : false }))
  )

  // Sync parent list into modal when it loads/updates (fixes list showing after API response)
  useEffect(() => {
    setLocalUsers(users.map((u) => ({ ...u, following: 'following' in u ? u.following : false })))
  }, [users])
  const [loading, setLoading] = useState(false)
  const [actionError, setActionError] = useState<string | null>(null)

  // Confirmation Modal State
  const [confirmUser, setConfirmUser] = useState<UserPreview | null>(null)
  const [confirmType, setConfirmType] = useState<'remove' | 'unfollow' | null>(null)

  const handleActionClick = (user: UserPreview & { following?: boolean }, type: 'remove' | 'unfollow') => {
    setConfirmUser(user)
    setConfirmType(type)
    setActionError(null)
  }

  const handleFollow = async (user: UserPreview & { following?: boolean }) => {
    setActionError(null)
    try {
      await followApi.follow(user.id)
      setLocalUsers((prev) => prev.map((u) => (u.id === user.id ? { ...u, following: true } : u)))
    } catch {
      setActionError(`Could not follow ${user.username}. Please try again.`)
    }
  }

  const handleConfirmAction = async () => {
    if (!confirmUser || !confirmType || loading) return
    setLoading(true)
    setActionError(null)

    try {
      if (confirmType === 'remove') {
        await followApi.removeFollower(confirmUser.id)
        setLocalUsers((prev) => prev.filter((u) => u.id !== confirmUser.id))
      } else if (confirmType === 'unfollow') {
        await followApi.unfollow(confirmUser.id)
        setLocalUsers((prev) => prev.map((u) => (u.id === confirmUser.id ? { ...u, following: false } : u)))
      }
      setConfirmUser(null)
      setConfirmType(null)
    } catch {
      setActionError(`Failed to ${confirmType} ${confirmUser.username}. Please try again.`)
    } finally {
      setLoading(false)
    }
  }

  return (
    <>
      <div className="modal-overlay" onClick={onClose} role="presentation">
        <div className="modal-box" onClick={(e) => e.stopPropagation()} role="dialog">
          <h3>{title}</h3>
          
          {actionError && (
            <div className="form-error" style={{ marginBottom: '1rem', padding: '0.5rem', background: 'var(--bg-card)' }}>
              {actionError}
            </div>
          )}

          <ul className="user-list">
            {localUsers.length === 0 && <li className="muted">No one yet.</li>}
            {localUsers.map((user) => (
              <li key={user.id} className="user-list-item" style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                <Link to={`/users/${user.id}`} onClick={onClose} style={{ flexGrow: 1, display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                  {user.profilePicture ? (
                    <img src={user.profilePicture} alt="" className="avatar" />
                  ) : (
                    <div className="avatar avatar-placeholder" />
                  )}
                  <span style={{ display: 'flex', flexDirection: 'column' }}>
                    <span>{user.username}</span>
                    {'name' in user && user.name && (
                      <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>{user.name}</span>
                    )}
                  </span>
                </Link>
                {isOwnList && kind === 'followers' && (
                  <button
                    type="button"
                    className="btn btn-secondary btn-sm"
                    onClick={(e) => {
                      e.stopPropagation()
                      handleActionClick(user, 'remove')
                    }}
                  >
                    Remove
                  </button>
                )}
                {isOwnList && kind === 'following' && (
                  <button
                    type="button"
                    className="btn btn-secondary btn-sm"
                    onClick={(e) => {
                      e.stopPropagation()
                      handleActionClick(user, 'unfollow')
                    }}
                  >
                    Unfollow
                  </button>
                )}
                {!isOwnList && (kind === 'followers' || kind === 'following' || kind === 'mutual') && currentUserId != null && user.id !== currentUserId && (
                  user.following ? (
                    <button
                      type="button"
                      className="btn btn-secondary btn-sm btn-following"
                      onClick={(e) => {
                        e.stopPropagation()
                        handleActionClick(user, 'unfollow')
                      }}
                    >
                      Following
                    </button>
                  ) : (
                    <button
                      type="button"
                      className="btn btn-primary btn-sm"
                      onClick={(e) => {
                        e.stopPropagation()
                        handleFollow(user)
                      }}
                    >
                      Follow
                    </button>
                  )
                )}
              </li>
            ))}
          </ul>
          <button type="button" className="btn btn-secondary modal-close" onClick={onClose}>
            Close
          </button>
        </div>
      </div>

      {!!confirmUser && !!confirmType && (
        <div className="modal-overlay" style={{ zIndex: 1100 }}>
          <div className="modal-box" style={{ maxWidth: 320, textAlign: 'center' }}>
            <div style={{ marginBottom: '1.5rem' }}>
              {confirmUser.profilePicture ? (
                <img src={confirmUser.profilePicture} alt="" className="avatar" style={{ width: 80, height: 80, margin: '0 auto 1rem' }} />
              ) : (
                <div className="avatar avatar-placeholder" style={{ width: 80, height: 80, margin: '0 auto 1rem', fontSize: '2rem' }} />
              )}
              {confirmType === 'remove' ? (
                <>
                  <h3 style={{ margin: '0 0 0.5rem' }}>Remove Follower?</h3>
                  <p className="muted" style={{ margin: 0, fontSize: '0.9rem' }}>
                    We won't tell {confirmUser.username} they were removed from your followers.
                  </p>
                </>
              ) : (
                <p style={{ margin: 0, fontWeight: 500 }}>Unfollow @{confirmUser.username}?</p>
              )}
            </div>
            
            <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
              <button
                type="button"
                className="btn btn-danger"
                style={{ width: '100%', fontWeight: 600 }}
                onClick={handleConfirmAction}
                disabled={loading}
              >
                {confirmType === 'remove' ? 'Remove' : 'Unfollow'}
              </button>
              <button
                type="button"
                className="btn btn-secondary"
                style={{ width: '100%' }}
                onClick={() => {
                  setConfirmUser(null)
                  setConfirmType(null)
                }}
                disabled={loading}
              >
                Cancel
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  )
}

