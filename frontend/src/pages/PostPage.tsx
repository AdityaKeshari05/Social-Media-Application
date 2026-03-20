import { useEffect, useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import { Link, useParams } from 'react-router-dom'
import { commentApi, postApi, profileApi } from '../api'
import type { CommentDto, PostDto, UserDto } from '../api'
import { UserListModal } from '../components/UserListModal'

export function PostPage() {
  const { id } = useParams<{ id: string }>()
  const postId = Number(id)

  const [post, setPost] = useState<PostDto | null>(null)
  const [comments, setComments] = useState<CommentDto[]>([])
  const [loading, setLoading] = useState(true)
  const [commentText, setCommentText] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [actionMessage, setActionMessage] = useState<string | null>(null)
  const [likedUsers, setLikedUsers] = useState<UserDto[]>([])
  const [isLiked, setIsLiked] = useState<boolean>(false)
  const [likeBusy, setLikeBusy] = useState(false)
  const [currentUsername, setCurrentUsername] = useState<string | null>(null)
  const [showLikesModal, setShowLikesModal] = useState(false)
  const [replyingToId, setReplyingToId] = useState<number | null>(null)
  const [replyText, setReplyText] = useState('')
  const [openReplies, setOpenReplies] = useState<Record<number, boolean>>({})

  const hasAuthToken = useMemo(() => !!localStorage.getItem('authToken'), [])

  useEffect(() => {
    let active = true
    ;(async () => {
      try {
        const [postRes, commentsRes, likedUsersRes] = await Promise.all([
          postApi.getById(postId),
          postApi.getComments(postId),
          postApi.likedUsers(postId),
        ])
        if (!active) return
        setPost(postRes.data)
        setComments(commentsRes.data)
        setLikedUsers(likedUsersRes.data)

        if (hasAuthToken) {
          try {
            const [likedRes, meRes] = await Promise.all([postApi.hasLiked(postId), profileApi.me()])
            if (!active) return
            setIsLiked(likedRes.data)
            setCurrentUsername(meRes.data.username)
          } catch (authErr) {
            console.error(authErr)
          }
        }
      } catch (err) {
        console.error(err)
        if (!active) return
        setError('Failed to load post')
      } finally {
        if (active) setLoading(false)
      }
    })()
    return () => {
      active = false
    }
  }, [postId, hasAuthToken])

  const refreshPostMeta = async () => {
    const [postRes, likedUsersRes] = await Promise.all([
      postApi.getById(postId),
      postApi.likedUsers(postId),
    ])
    setPost(postRes.data)
    setLikedUsers(likedUsersRes.data)
    if (hasAuthToken) {
      try {
        const likedRes = await postApi.hasLiked(postId)
        setIsLiked(likedRes.data)
      } catch (err) {
        console.error(err)
      }
    }
  }

  const handleToggleLike = async () => {
    if (!hasAuthToken) {
      setActionMessage('Please log in to like posts')
      return
    }
    if (likeBusy) return
    setLikeBusy(true)
    try {
      if (isLiked) {
        await postApi.unlike(postId)
        setActionMessage('Unliked')
      } else {
        await postApi.like(postId)
        setActionMessage('Liked')
      }
      await refreshPostMeta()
    } catch (err) {
      console.error(err)
      setActionMessage('Action failed')
    } finally {
      setLikeBusy(false)
    }
  }

  const handleSubmitComment = async (e: FormEvent) => {
    e.preventDefault()
    if (!commentText.trim()) return
    try {
      const { data } = await postApi.addComment(postId, commentText.trim())
      setComments((prev) => [data, ...prev])
      setCommentText('')
    } catch (err) {
      console.error(err)
      setActionMessage('Failed to add comment')
    }
  }

  const handleSubmitReply = async (e: FormEvent, parentCommentId: number) => {
    e.preventDefault()
    if (!replyText.trim()) return
    try {
      const { data } = await postApi.addComment(postId, replyText.trim(), parentCommentId)
      setComments((prev) => [data, ...prev])
      setReplyText('')
      setReplyingToId(null)
      setOpenReplies((prev) => ({ ...prev, [parentCommentId]: true }))
    } catch (err) {
      console.error(err)
      setActionMessage('Failed to add reply')
    }
  }

  if (loading) {
    return <div className="page-centered">Loading post...</div>
  }

  if (error || !post) {
    return <div className="page-centered error">{error ?? 'Post not found'}</div>
  }

  const likeCount = post.noOfLikes ?? 0
  const likesForModal = likedUsers.map((u) => ({
    id: u.id,
    username: u.username,
    profilePicture: (u as { profilePicture?: string }).profilePicture,
  }))

  return (
    <div className="page">
      <article className="post-detail">
        <header className="post-header">
          <div className="post-author">
            {post.user?.id ? (
              <Link to={`/users/${post.user.id}`}>
                {post.profilePicture && (
                  <img src={post.profilePicture} alt={post.user.username} className="avatar" />
                )}
                <div>
                  <div className="post-username">{post.user.username}</div>
                  {post.categoryName && <div className="post-meta">{post.categoryName}</div>}
                </div>
              </Link>
            ) : (
              <>
                {post.profilePicture && (
                  <img src={post.profilePicture} alt={post.user?.username} className="avatar" />
                )}
                <div>
                  <div className="post-username">{post.user?.username}</div>
                  {post.categoryName && <div className="post-meta">{post.categoryName}</div>}
                </div>
              </>
            )}
          </div>
          {currentUsername && post.user?.username === currentUsername && (
            <Link className="btn btn-secondary" to={`/posts/${postId}/edit`}>
              Edit
            </Link>
          )}
        </header>
        <h1 className="post-title">{post.title}</h1>
        <p className="post-content">{post.content}</p>
        {post.postImage && (
          <div className="post-image-wrapper">
            <img src={post.postImage} alt={post.title} className="post-image" />
          </div>
        )}
        <footer className="post-footer post-footer-actions">
          <button
            type="button"
            className={`btn ${isLiked ? 'btn-liked' : 'btn-primary'}`}
            onClick={handleToggleLike}
            disabled={likeBusy}
          >
            {isLiked ? 'Liked' : 'Like'}
          </button>
          <button
            type="button"
            className="likes-count-btn"
            onClick={() => setShowLikesModal(true)}
          >
            {likeCount} {likeCount === 1 ? 'like' : 'likes'}
          </button>
          <span className="comments-count-span">
            {post.noOfComments ?? 0} comments
          </span>
        </footer>
        {actionMessage && <div className="inline-message">{actionMessage}</div>}
      </article>

      {showLikesModal && (
        <UserListModal
          title="Liked by"
          users={likesForModal}
          onClose={() => setShowLikesModal(false)}
        />
      )}

      <section className="comments-section">
        <h2>Comments</h2>
        <form className="comment-form" onSubmit={handleSubmitComment}>
          <textarea
            value={commentText}
            onChange={(e) => setCommentText(e.target.value)}
            placeholder="Add a comment..."
          />
          <button type="submit" className="btn btn-primary">
            Comment
          </button>
        </form>
        <div className="comment-list">
          {comments.length === 0 && <div className="muted">No comments yet.</div>}
          {comments
            .filter((c) => !c.parentCommentId)
            .map((comment) => {
              const replies = comments.filter((c) => c.parentCommentId === comment.id)
              const hasReplies = replies.length > 0
              const isOpen = openReplies[comment.id] ?? false
              return (
            <div key={comment.id} className="comment-item">
              <div className="comment-header">
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  {comment.user?.profilePicture ? (
                    <img src={comment.user.profilePicture} alt={comment.user.username} className="avatar" />
                  ) : (
                    <div className="avatar avatar-placeholder" />
                  )}
                  {comment.user?.id ? (
                    <Link to={`/users/${comment.user.id}`}>{comment.user.username}</Link>
                  ) : (
                    <span className="comment-username">{comment.user?.username}</span>
                  )}
                </div>
                {comment.createdAt && (
                  <div className="comment-date">
                    {new Date(comment.createdAt).toLocaleString()}
                  </div>
                )}
              </div>
              <p className="comment-content">{comment.text}</p>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginTop: '0.25rem' }}>
                <button
                  type="button"
                  className="btn btn-ghost"
                  onClick={async () => {
                    try {
                      const alreadyLiked = (comment as any)._liked === true
                      if (alreadyLiked) {
                        await commentApi.unlike(comment.id)
                        setComments((prev) =>
                          prev.map((c) =>
                            c.id === comment.id
                              ? { ...c, likesCount: (c.likesCount ?? 1) - 1, _liked: false as any }
                              : c,
                          ),
                        )
                      } else {
                        await commentApi.like(comment.id)
                        setComments((prev) =>
                          prev.map((c) =>
                            c.id === comment.id
                              ? { ...c, likesCount: (c.likesCount ?? 0) + 1, _liked: true as any }
                              : c,
                          ),
                        )
                      }
                    } catch (err) {
                      console.error(err)
                      // Keep UI stable; quietly ignore transient errors
                    }
                  }}
                >
                  Like
                </button>
                <span className="muted">{comment.likesCount ?? 0} likes</span>
                <button
                  type="button"
                  className="btn btn-ghost"
                  onClick={() => {
                    const uname = comment.user?.username
                    if (uname) {
                      setReplyingToId(comment.id)
                      setReplyText((prev) => (prev ? prev : `@${uname} `))
                    }
                  }}
                >
                  Reply
                </button>
              </div>
              {currentUsername && comment.user?.username === currentUsername && (
                <button
                  type="button"
                  className="btn btn-ghost"
                  onClick={async () => {
                    try {
                      await commentApi.delete(comment.id)
                      setComments((prev) => prev.filter((c) => c.id !== comment.id))
                      setActionMessage('Comment deleted')
                    } catch (err) {
                      console.error(err)
                      setActionMessage('Failed to delete comment')
                    }
                  }}
                >
                  Delete
                </button>
              )}
              {hasReplies && (
                <button
                  type="button"
                  className="link-button"
                  onClick={() =>
                    setOpenReplies((prev) => ({ ...prev, [comment.id]: !isOpen }))
                  }
                  style={{ marginTop: '0.25rem' }}
                >
                  {isOpen ? 'Hide replies' : `Replies (${replies.length})`}
                </button>
              )}
              {replyingToId === comment.id && (
                <form
                  className="comment-form"
                  onSubmit={(e) => handleSubmitReply(e, comment.id)}
                  style={{ marginTop: '0.5rem' }}
                >
                  <textarea
                    value={replyText}
                    onChange={(e) => setReplyText(e.target.value)}
                    placeholder={`Reply to @${comment.user?.username ?? 'user'}...`}
                  />
                  <div style={{ display: 'flex', gap: '0.5rem' }}>
                    <button type="submit" className="btn btn-primary">
                      Reply
                    </button>
                    <button
                      type="button"
                      className="btn btn-secondary"
                      onClick={() => {
                        setReplyingToId(null)
                        setReplyText('')
                      }}
                    >
                      Cancel
                    </button>
                  </div>
                </form>
              )}
              {isOpen && replies.length > 0 && (
                <div style={{ marginTop: '0.5rem', paddingLeft: '2.5rem', borderLeft: '1px solid var(--border-subtle)' }}>
                  {replies.map((reply) => (
                    <div key={reply.id} className="comment-item" style={{ marginBottom: '0.4rem' }}>
                      <div className="comment-header">
                        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                          {reply.user?.profilePicture ? (
                            <img src={reply.user.profilePicture} alt={reply.user.username} className="avatar" />
                          ) : (
                            <div className="avatar avatar-placeholder" />
                          )}
                          {reply.user?.id ? (
                            <Link to={`/users/${reply.user.id}`}>{reply.user.username}</Link>
                          ) : (
                            <span className="comment-username">{reply.user?.username}</span>
                          )}
                        </div>
                        {reply.createdAt && (
                          <div className="comment-date">
                            {new Date(reply.createdAt).toLocaleString()}
                          </div>
                        )}
                      </div>
                      <p className="comment-content">{reply.text}</p>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginTop: '0.25rem' }}>
                        <button
                          type="button"
                          className="btn btn-ghost"
                          onClick={async () => {
                            try {
                              const alreadyLiked = (reply as any)._liked === true
                              if (alreadyLiked) {
                                await commentApi.unlike(reply.id)
                                setComments((prev) =>
                                  prev.map((c) =>
                                    c.id === reply.id
                                      ? { ...c, likesCount: (c.likesCount ?? 1) - 1, _liked: false as any }
                                      : c,
                                  ),
                                )
                              } else {
                                await commentApi.like(reply.id)
                                setComments((prev) =>
                                  prev.map((c) =>
                                    c.id === reply.id
                                      ? { ...c, likesCount: (c.likesCount ?? 0) + 1, _liked: true as any }
                                      : c,
                                  ),
                                )
                              }
                            } catch (err) {
                              console.error(err)
                            }
                          }}
                        >
                          Like
                        </button>
                        <span className="muted">{reply.likesCount ?? 0} likes</span>
                        <button
                          type="button"
                          className="btn btn-ghost"
                          onClick={() => {
                            const replyUname = reply.user?.username
                            if (replyUname) {
                              // Reply to this reply but keep the thread grouped under the original comment
                              setReplyingToId(comment.id)
                              setReplyText((prev) => (prev ? prev : `@${replyUname} `))
                            }
                          }}
                        >
                          Reply
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
              )
            })}
        </div>
      </section>
    </div>
  )
}
