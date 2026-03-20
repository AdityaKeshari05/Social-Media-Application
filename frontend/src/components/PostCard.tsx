import { useState } from 'react'
import { Link } from 'react-router-dom'
import type { PostDto } from '../api'

/** Default preview: below 20 words (then "more" to expand). */
const CONTENT_PREVIEW_WORDS = 20

function getFirstNWords(text: string, n: number): { preview: string; isLong: boolean } {
  const words = text.trim().split(/\s+/).filter(Boolean)
  const isLong = words.length > n
  const preview = isLong ? words.slice(0, n).join(' ') : text.trim()
  return { preview, isLong }
}

interface PostCardProps {
  post: PostDto
}

export function PostCard({ post }: PostCardProps) {
  const userId = post.user?.id
  const content = post.content ?? ''
  const { preview, isLong } = getFirstNWords(content, CONTENT_PREVIEW_WORDS)
  const [expanded, setExpanded] = useState(false)
  const displayContent = expanded ? content : preview

  return (
    <article className="post-card">
      <header className="post-header">
        <div className="post-author">
          {userId ? (
            <Link to={`/users/${userId}`}>
              {post.profilePicture && (
                <img src={post.profilePicture} alt={post.user?.username} className="avatar" />
              )}
              <div>
                <div className="post-username">{post.user?.username}</div>
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
        {post.createdAt && (
          <div className="post-date">{new Date(post.createdAt).toLocaleString()}</div>
        )}
      </header>
      <Link to={`/posts/${post.id}`} className="post-body">
        <h2 className="post-title">{post.title}</h2>
        <div className="post-content-wrap">
          <p className="post-content">
            {displayContent}
            {!expanded && isLong && (
              <>
                {' … '}
                <button
                  type="button"
                  className="post-content-more"
                  onClick={(e) => {
                    e.preventDefault()
                    e.stopPropagation()
                    setExpanded(true)
                  }}
                >
                  more
                </button>
              </>
            )}
          </p>
        </div>
        {post.postImage && (
          <div className="post-image-wrapper">
            <img src={post.postImage} alt={post.title} className="post-image" />
          </div>
        )}
      </Link>
      <footer className="post-footer">
        <span>{post.noOfLikes ?? 0} likes</span>
        <span>{post.noOfComments ?? 0} comments</span>
      </footer>
    </article>
  )
}
