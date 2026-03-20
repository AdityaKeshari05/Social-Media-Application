import { useCallback, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { searchApi } from '../api'
import type { UserPreview } from '../api'

const SEARCH_DEBOUNCE_MS = 300
const MIN_QUERY_LENGTH = 1

export function SearchPage() {
  const [query, setQuery] = useState('')
  const [results, setResults] = useState<UserPreview[]>([])
  const [loading, setLoading] = useState(false)

  const runSearch = useCallback(async (q: string) => {
    if (!q || q.length < MIN_QUERY_LENGTH) {
      setResults([])
      return
    }
    setLoading(true)
    try {
      const { data } = await searchApi.searchUsers(q)
      setResults(data)
    } catch {
      setResults([])
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    const t = setTimeout(() => runSearch(query), SEARCH_DEBOUNCE_MS)
    return () => clearTimeout(t)
  }, [query, runSearch])

  return (
    <div className="page search-page">
      <h1 className="page-title">Search users</h1>
      <div className="search-page-input-wrap">
        <span className="search-icon" aria-hidden="true">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <circle cx="11" cy="11" r="8" />
            <path d="m21 21-4.35-4.35" />
          </svg>
        </span>
        <input
          type="search"
          className="search-page-input"
          placeholder="Search by username..."
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          aria-label="Search users"
          autoComplete="off"
          autoFocus
        />
      </div>

      {loading && <div className="muted">Searching...</div>}
      {!loading && query.length >= MIN_QUERY_LENGTH && results.length === 0 && (
        <div className="muted">No users found for &quot;{query}&quot;</div>
      )}
      {!loading && results.length > 0 && (
        <ul className="search-results-list">
          {results.map((user) => {
            const isFollowing = Boolean(user.following)
            const hasFollowSummary = Boolean(user.followedBySummary && user.followedBySummary.trim())
            const showFollowersCount = !isFollowing && typeof user.followersCount === 'number'

            return (
              <li key={user.id}>
                <Link to={`/users/${user.id}`} className="search-result-item">
                  {user.profilePicture ? (
                    <img src={user.profilePicture} alt="" className="avatar search-result-avatar" />
                  ) : (
                    <span className="avatar search-result-avatar search-avatar-placeholder">
                      {user.username.charAt(0).toUpperCase()}
                    </span>
                  )}
                  <div className="search-result-main">
                    <div className="search-result-username">{user.username}</div>
                    {user.name && <div className="search-result-name">{user.name}</div>}
                    {isFollowing && hasFollowSummary ? (
                      <div className="search-result-meta">{user.followedBySummary}</div>
                    ) : showFollowersCount ? (
                      <div className="search-result-meta">
                        {user.followersCount} follower{user.followersCount === 1 ? '' : 's'}
                      </div>
                    ) : null}
                  </div>
                </Link>
              </li>
            )
          })}
        </ul>
      )}
    </div>
  )
}
