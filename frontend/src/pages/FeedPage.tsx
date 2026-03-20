import { useEffect, useState, useRef, useCallback } from 'react'
import { postApi } from '../api'
import type { PostDto } from '../api'
import { PostCard } from '../components/PostCard'


const PAGE_SIZE = 15

export function FeedPage() {
  const [posts, setPosts] = useState<PostDto[]>([])
  const [page, setPage] = useState(0)
  const [hasMore, setHasMore] = useState(true)
  const [loading, setLoading] = useState(true)
  const [loadingMore, setLoadingMore] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const seedRef = useRef<string>(`${Date.now()}-${Math.random()}`)
  const sentinelRef = useRef<HTMLDivElement>(null)
  const loadingMoreRef = useRef(false)

  const loadMore = useCallback(async () => {
    if (loadingMoreRef.current || !hasMore) return
    loadingMoreRef.current = true
    setLoadingMore(true)
    try {
      const nextPage = page + 1
      const { data } = await postApi.getAll(nextPage, PAGE_SIZE, seedRef.current)
      setPosts((prev) => [...prev, ...data.content])
      setPage(nextPage)
      setHasMore(!data.last)
    } catch (err) {
      console.error(err)
    } finally {
      loadingMoreRef.current = false
      setLoadingMore(false)
    }
  }, [page, hasMore])

  useEffect(() => {
    let active = true
    ;(async () => {
      try {
        const { data } = await postApi.getAll(0, PAGE_SIZE, seedRef.current)
        if (!active) return
        setPosts(data.content)
        setPage(0)
        setHasMore(!data.last)
      } catch (err) {
        console.error(err)
        if (!active) return
        setError('Failed to load posts')
      } finally {
        if (active) setLoading(false)
      }
    })()
    return () => {
      active = false
    }
  }, [])

  useEffect(() => {
    const sentinel = sentinelRef.current
    if (!sentinel || !hasMore || loading) return

    const observer = new IntersectionObserver(
      (entries) => {
        const [entry] = entries
        if (entry?.isIntersecting && hasMore && !loadingMore) {
          loadMore()
        }
      },
      { root: null, rootMargin: '100px', threshold: 0.1 }
    )

    observer.observe(sentinel)
    return () => observer.disconnect()
  }, [hasMore, loading, loadingMore, loadMore])

  if (loading) {
    return <div className="page-centered">Loading feed...</div>
  }

  if (error) {
    return <div className="page-centered error">{error}</div>
  }

  if (posts.length === 0) {
    return <div className="page-centered">No posts yet.</div>
  }

  return (
    <div className="page">
      <h1 className="page-title">Feed</h1>
      <div className="feed-grid">
        <div className="feed-main">
          {posts.map((post) => (
            <PostCard key={post.id} post={post} />
          ))}
          <div ref={sentinelRef} style={{ height: 1, marginTop: 8 }} />
          {loadingMore && (
            <div className="page-centered" style={{ padding: '1rem' }}>
              Loading more posts...
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

