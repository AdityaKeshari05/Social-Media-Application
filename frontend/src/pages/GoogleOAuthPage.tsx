import { useEffect, useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { authApi } from '../api'
import { useAuth } from '../AuthContext'

function useQueryParams() {
  const { search } = useLocation()
  return useMemo(() => new URLSearchParams(search), [search])
}

export function GoogleOAuthPage() {
  const navigate = useNavigate()
  const { loginWithToken } = useAuth()
  const qp = useQueryParams()

  const token = qp.get('token')
  const email = qp.get('email') ?? ''
  const name = qp.get('name') ?? ''

  const [username, setUsername] = useState(() => (name ? name.replace(/\s+/g, '').toLowerCase() : ''))
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!token) return
    loginWithToken(token)
    navigate('/', { replace: true })
  }, [token, loginWithToken, navigate])

  const isRegisterFlow = !token && !!email

  const handleRegister = async (e: FormEvent) => {
    e.preventDefault()
    setError(null)
    setLoading(true)
    try {
      const { data } = await authApi.oauth2Register({ email, username })
      loginWithToken(data.token)
      navigate('/', { replace: true })
    } catch {
      setError('Could not complete Google signup. Please try a different username.')
    } finally {
      setLoading(false)
    }
  }

  if (token) {
    return <div className="page-centered">Signing you in…</div>
  }

  if (!isRegisterFlow) {
    return <div className="page-centered error">Google sign-in failed. Please try again.</div>
  }

  return (
    <div className="page auth-page">
      <div className="auth-card">
        <h1 className="page-title">Finish signup</h1>
        <div className="muted" style={{ marginBottom: '0.75rem' }}>
          Verified Google email: <strong>{email}</strong>
        </div>
        <form className="auth-form" onSubmit={handleRegister}>
          <label>
            <span>Username</span>
            <input
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
              minLength={3}
            />
          </label>
          {error && <div className="form-error">{error}</div>}
          <button type="submit" className="btn btn-primary" disabled={loading}>
            {loading ? 'Creating account…' : 'Create account'}
          </button>
        </form>
      </div>
    </div>
  )
}

