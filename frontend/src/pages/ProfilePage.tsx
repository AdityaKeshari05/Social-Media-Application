import { useEffect, useState } from 'react'
import { Link, useParams, useNavigate } from 'react-router-dom'
import { UserListModal } from '../components/UserListModal'
import { followApi, profileApi } from '../api'
import type { PostDto, ProfileDto, UserPreview, UserPreviewWithFollowStatus } from '../api'

export function ProfilePage() {
  const params = useParams<{ id?: string }>()
  const userId = params.id ? Number(params.id) : null

  const [profile, setProfile] = useState<ProfileDto | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [followMessage, setFollowMessage] = useState<string | null>(null)
  const [confirmAction, setConfirmAction] = useState<'unfollow' | 'cancel_request' | null>(null)
  const [confirmLoading, setConfirmLoading] = useState(false)
  const [confirmError, setConfirmError] = useState<string | null>(null)
  const [listModal, setListModal] = useState<'followers' | 'following' | 'mutual' | null>(null)
  const [listUsers, setListUsers] = useState<(UserPreview | UserPreviewWithFollowStatus)[]>([])
  const [listLoading, setListLoading] = useState(false)
  const [showAvatarModal, setShowAvatarModal] = useState(false)
  const navigate = useNavigate()

  const [viewerUserId, setViewerUserId] = useState<number | null>(null)

  const profileUserId = profile?.userId ?? userId

  const normalizeProfile = (data: unknown): ProfileDto => {
    const d = data as any
    return {
      ...d,
      isPrivate: d?.isPrivate ?? d?.private ?? false,
      isFollowing: d?.isFollowing ?? d?.following ?? false,
      isFollowRequested: d?.isFollowRequested ?? d?.followRequested ?? d?.requestPending ?? false,
    } as ProfileDto
  }

  useEffect(() => {
    let active = true
    ;(async () => {
      try {
        const { data } = userId
          ? await profileApi.byUserId(userId)
          : await profileApi.me()
        if (!active) return
        setProfile(normalizeProfile(data))
        if (!userId) {
          // When viewing /me, this is also the viewer id.
          setViewerUserId(data.userId ?? null)
        }
      } catch (err) {
        console.error(err)
        if (!active) return
        setError('Failed to load profile')
      } finally {
        if (active) setLoading(false)
      }
    })()
    return () => {
      active = false
    }
  }, [userId])

  // Fetch current viewer id once (used to detect "my own profile" even on /users/{id} routes)
  useEffect(() => {
    let active = true
    ;(async () => {
      try {
        const { data } = await profileApi.me()
        if (!active) return
        setViewerUserId(data.userId ?? null)
      } catch {
        // not logged in or request failed; ignore
      }
    })()
    return () => {
      active = false
    }
  }, [])

  const openList = async (kind: 'followers' | 'following' | 'mutual') => {
    if (!profileUserId) return
    setListModal(kind)
    setListLoading(true)
    setListUsers([])
    try {
      if (kind === 'mutual') {
        const { data } = await followApi.mutualFollowers(profileUserId)
        setListUsers(data)
      } else {
        const fn = userId
          ? (kind === 'followers' ? followApi.followersOfUser(profileUserId) : followApi.followingOfUser(profileUserId))
          : (kind === 'followers' ? followApi.myFollowers() : followApi.myFollowing())
        const { data } = await fn
        setListUsers(data)
      }
    } catch (err) {
      console.error(err)
      setListUsers([])
    } finally {
      setListLoading(false)
    }
  }

  if (loading) {
    return <div className="page-centered">Loading profile...</div>
  }

  if (error || !profile) {
    return <div className="page-centered error">{error ?? 'Profile not found'}</div>
  }

  const posts: PostDto[] = profile.posts || []
  const isMe =
    (!!profile && viewerUserId != null && (
      !userId || // /me route
      viewerUserId === userId || // /users/{id} matches current viewer
      (profile.userId != null && viewerUserId === profile.userId) // backend-provided userId matches
    ))
  // Show followers/following when: own profile, or public account, or we follow them
  const canShowFollowLists = isMe || !profile.isPrivate || Boolean(profile.isFollowing)
  const canViewPosts = isMe || !profile.isPrivate || Boolean(profile.isFollowing)

  return (
    <div className="page profile-page">
      <section className="profile-header">
        <div className="profile-avatar-wrap">
          {profile.profilePicture ? (
            <button
              type="button"
              className="profile-avatar-button"
              onClick={() => setShowAvatarModal(true)}
              aria-label="View profile picture"
            >
              <img src={profile.profilePicture} alt={profile.username} className="profile-avatar" />
            </button>
          ) : (
            <div className="profile-avatar profile-avatar-placeholder" />
          )}
        </div>
        <div className="profile-header-info">
          <div className="profile-header-top">
            <h1 className="profile-username">{profile.username}</h1>
          </div>
          {(profile.name != null && profile.name !== '') && (
            <p className="profile-name">{profile.name}</p>
          )}
          <p className="profile-bio">{profile.bio}</p>
          {((profile.website != null && profile.website !== '') || (profile.link2 != null && profile.link2 !== '')) && (
            <div className="profile-links">
              {profile.website != null && profile.website !== '' && (
                <a href={profile.website.startsWith('http') ? profile.website : `https://${profile.website}`} target="_blank" rel="noopener noreferrer" className="profile-link">
                  <span className="profile-link-icon" data-site={/linkedin\.com/i.test(profile.website) ? 'linkedin' : /twitter\.com|x\.com/i.test(profile.website) ? 'twitter' : /github\.com/i.test(profile.website) ? 'github' : 'link'} aria-hidden />
                  <span className="profile-link-text">{profile.website.replace(/^https?:\/\//i, '')}</span>
                </a>
              )}
              {profile.link2 != null && profile.link2 !== '' && (
                <a href={profile.link2.startsWith('http') ? profile.link2 : `https://${profile.link2}`} target="_blank" rel="noopener noreferrer" className="profile-link">
                  <span className="profile-link-icon" data-site={/linkedin\.com/i.test(profile.link2) ? 'linkedin' : /twitter\.com|x\.com/i.test(profile.link2) ? 'twitter' : /github\.com/i.test(profile.link2) ? 'github' : 'link'} aria-hidden />
                  <span className="profile-link-text">{profile.link2.replace(/^https?:\/\//i, '')}</span>
                </a>
              )}
            </div>
          )}
          {userId && profile.mutualPreview && profile.mutualPreview.length > 0 && (
            <p className="profile-mutual">
              Followed by{' '}
              <button
                type="button"
                className="link-button"
                onClick={() => openList('mutual')}
                style={{ padding: 0, fontWeight: 500 }}
              >
                {profile.mutualPreview.length === 1
                  ? profile.mutualPreview[0].username
                  : (profile.mutualCount ?? 0) > 2
                    ? `${profile.mutualPreview[0].username}, ${profile.mutualPreview[1].username} and ${(profile.mutualCount ?? 0) - 2} others`
                    : `${profile.mutualPreview[0].username} and ${profile.mutualPreview[1].username}`}
              </button>
            </p>
          )}
          <div className="profile-stats">
            <span className="stat-item"><strong>{profile.noOfPosts}</strong> Posts</span>
            {canShowFollowLists && profileUserId && (
              <>
                <span className="stat-divider">|</span>
                <button type="button" className="link-button stat-item" onClick={() => openList('followers')}>
                  <strong>{profile.followersCount ?? 0}</strong> Followers
                </button>
                <span className="stat-divider">|</span>
                <button type="button" className="link-button stat-item" onClick={() => openList('following')}>
                  <strong>{profile.followingCount ?? 0}</strong> Following
                </button>
              </>
            )}
            {profile.isPrivate && (
              <>
                <span className="stat-divider">|</span>
                <span className="badge stat-badge">
                  <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" style={{marginRight: '6px', verticalAlign: 'text-bottom'}}><rect width="18" height="11" x="3" y="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
                  Private
                </span>
              </>
            )}
          </div>
          {isMe && (
            <div className="profile-edit-row">
              <Link to="/profile/edit" className="btn-edit-profile">
                Edit profile
              </Link>
            </div>
          )}

          {userId && !isMe && (
            <div className="profile-actions-row">
              {profile.isFollowing ? (
                <button
                  type="button"
                  className="btn btn-secondary btn-following"
                  onClick={() => setConfirmAction('unfollow')}
                >
                  Following
                </button>
              ) : profile.isFollowRequested ? (
                <button
                  type="button"
                  className="btn btn-secondary btn-following"
                  onClick={() => setConfirmAction('cancel_request')}
                >
                  Sent
                </button>
              ) : (
                <button
                  type="button"
                  className="btn btn-primary"
                  onClick={async () => {
                    try {
                      const { data } = await followApi.follow(userId)
                      setFollowMessage(typeof data === 'object' && data?.message ? data.message : String(data))
                      setProfile((p) => {
                        if (!p) return null
                        if (p.isPrivate) {
                          return { ...p, isFollowRequested: true, isFollowing: false }
                        }
                        return { ...p, isFollowing: true, isFollowRequested: false }
                      })
                    } catch {
                      setFollowMessage('Could not send follow request')
                    }
                  }}
                >
                  Follow
                </button>
              )}

              {profile.isFollowing && (
                <button
                  type="button"
                  className="btn btn-secondary"
                  onClick={() => navigate(`/messages?userId=${userId}`)}
                >
                  Message
                </button>
              )}
            </div>
          )}
          {followMessage && <div className="inline-message">{followMessage}</div>}
        </div>
      </section>

      {confirmAction && (
        <div
          className="modal-overlay"
          onClick={() => {
            if (confirmLoading) return
            setConfirmAction(null)
            setConfirmError(null)
          }}
          role="presentation"
        >
          <div className="modal-box" onClick={(e) => e.stopPropagation()} role="dialog" aria-label="Unfollow confirmation">
            {confirmAction === 'unfollow' ? (
              <>
                <h3>Unfollow {profile.username}?</h3>
                <p className="muted">They will no longer see your posts in their feed.</p>
              </>
            ) : (
              <>
                <h3>Delete follow request?</h3>
                <p className="muted">This will cancel your pending follow request to {profile.username}.</p>
              </>
            )}
            {confirmError && <div className="form-error">{confirmError}</div>}
            <div className="modal-actions">
              <button
                type="button"
                className="btn btn-secondary"
                onClick={() => {
                  if (confirmLoading) return
                  setConfirmAction(null)
                  setConfirmError(null)
                }}
              >
                Cancel
              </button>
              <button
                type="button"
                className={`btn btn-primary ${confirmAction === 'unfollow' ? 'btn-danger' : ''}`}
                onClick={async () => {
                  if (!userId) return
                  if (confirmLoading) return
                  setConfirmLoading(true)
                  setConfirmError(null)
                  try {
                    if (confirmAction === 'unfollow') {
                      const { data } = await followApi.unfollow(userId)
                      setProfile((p) => (p ? { ...p, isFollowing: false, isFollowRequested: false } : null))
                      setFollowMessage(data?.message ?? 'Unfollowed')
                    } else {
                      const { data } = await followApi.cancelRequest(userId)
                      setProfile((p) => (p ? { ...p, isFollowRequested: false } : null))
                      setFollowMessage(data?.message ?? 'Request cancelled')
                    }
                    setConfirmAction(null)
                    setConfirmError(null)
                  } catch {
                    setConfirmError(confirmAction === 'unfollow' ? 'Could not unfollow. Please try again.' : 'Could not delete request. Please try again.')
                  } finally {
                    setConfirmLoading(false)
                  }
                }}
                disabled={confirmLoading}
              >
                {confirmLoading ? 'Please wait…' : (confirmAction === 'unfollow' ? 'Unfollow' : 'Delete request')}
              </button>
            </div>
          </div>
        </div>
      )}

      {canViewPosts && (
        <section className="profile-posts-section">
          <h2 className="section-title">Posts</h2>
          {posts.length === 0 && <div className="muted">No posts yet.</div>}
          {posts.length > 0 ? (
            <div className="profile-posts-grid" role="list">
              {posts.map((post) => (
                <button
                  key={post.id}
                  type="button"
                  className="profile-post-thumb"
                  onClick={() => navigate(`/posts/${post.id}`)}
                  aria-label={`View post: ${post.title}`}
                >
                  {post.postImage ? (
                    <img src={post.postImage} alt="" />
                  ) : (
                    <div className="profile-post-thumb-placeholder">
                      <span>{post.title || 'Post'}</span>
                    </div>
                  )}
                </button>
              ))}
            </div>
          ) : null}
        </section>
      )}
      {!canViewPosts && profile.isPrivate && (
        <div className="muted">
          This account is private. Follow to request access; they can accept to let you see their posts and follow lists.
        </div>
      )}

      {listModal && (
        <UserListModal
          title={listModal === 'followers' ? 'Followers' : listModal === 'following' ? 'Following' : 'Followed by'}
          users={listLoading ? [] : listUsers}
          onClose={() => { setListModal(null); setListUsers([]) }}
          kind={listModal}
          isOwnList={isMe && listModal !== 'mutual'}
          currentUserId={viewerUserId}
        />
      )}
      {showAvatarModal && profile.profilePicture && (
        <div
          className="modal-overlay"
          onClick={() => setShowAvatarModal(false)}
          role="presentation"
        >
          <div
            className="modal-box profile-avatar-modal"
            onClick={(e) => e.stopPropagation()}
            role="dialog"
            aria-label="Profile picture"
          >
            <img src={profile.profilePicture} alt={profile.username} className="profile-avatar-large" />
          </div>
        </div>
      )}
    </div>
  )
}


// npm run dev
// cd frontend 
// powershell -ExecutionPolicy Bypass -File .\h.ps1