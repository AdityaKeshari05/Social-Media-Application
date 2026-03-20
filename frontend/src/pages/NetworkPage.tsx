import { useEffect, useState } from 'react'
import { followApi } from '../api'
import type { UserPreview } from '../api'
import { UserListModal } from '../components/UserListModal'

export function NetworkPage() {
  const [followers, setFollowers] = useState<UserPreview[]>([])
  const [following, setFollowing] = useState<UserPreview[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let active = true
    ;(async () => {
      try {
        const [followersRes, followingRes] = await Promise.all([
          followApi.myFollowers(),
          followApi.myFollowing(),
        ])
        if (!active) return
        setFollowers(followersRes.data)
        setFollowing(followingRes.data)
      } catch (err) {
        console.error(err)
        if (!active) return
        setError('Failed to load network')
      } finally {
        if (active) setLoading(false)
      }
    })()
    return () => {
      active = false
    }
  }, [])

  if (loading) {
    return <div className="page-centered">Loading network...</div>
  }

  if (error) {
    return <div className="page-centered error">{error}</div>
  }

  return (
    <div className="page">
      <h1 className="page-title">Your network</h1>
      <div className="network-grid" style={{ display: 'flex', gap: '2rem', flexWrap: 'wrap' }}>
        <section style={{ flex: '1 1 300px', minWidth: 300 }}>
          <div style={{ padding: '1rem', border: '1px solid var(--border)', borderRadius: 'var(--radius)', background: 'var(--bg-card)', position: 'relative' }}>
            <UserListModal 
              title="Followers" 
              users={followers} 
              onClose={() => {}} 
              kind="followers"
              isOwnList={true}
            />
          </div>
        </section>
        <section style={{ flex: '1 1 300px', minWidth: 300 }}>
          <div style={{ padding: '1rem', border: '1px solid var(--border)', borderRadius: 'var(--radius)', background: 'var(--bg-card)', position: 'relative' }}>
            <UserListModal 
              title="Following" 
              users={following} 
              onClose={() => {}} 
              kind="following"
              isOwnList={true}
            />
          </div>
        </section>
      </div>
    </div>
  )
}

