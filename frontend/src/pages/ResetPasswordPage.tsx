import { useState } from 'react'
import type { FormEvent } from 'react'
import { useNavigate, useSearchParams, Link } from 'react-router-dom'
import { authApi } from '../api'

export function ResetPasswordPage() {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const token = searchParams.get('token')

  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault()
    setError(null)

    if (newPassword !== confirmPassword) {
      setError('Passwords do not match.')
      return
    }

    if (newPassword.length < 6) {
      setError('Password must be at least 6 characters.')
      return
    }

    if (!token) {
      setError('Invalid or missing reset link.')
      return
    }

    setLoading(true)
    try {
      await authApi.resetPassword(token, newPassword)
      navigate('/login', { replace: true })
    } catch {
      setError('This link has expired or is invalid. Please request a new one.')
    } finally {
      setLoading(false)
    }
  }

  if (!token) {
    return (
      <div className="page auth-page">
        <div className="auth-card">
          <h1 className="page-title">Invalid link</h1>
          <p className="muted">This reset link is invalid or missing. Please request a new password reset.</p>
          <Link to="/forgot-password" className="btn btn-primary" style={{ marginTop: '1rem', display: 'inline-block' }}>
            Request new link
          </Link>
        </div>
      </div>
    )
  }

  return (
    <div className="page auth-page">
      <div className="auth-card">
        <h1 className="page-title">Set new password</h1>
        <p className="muted" style={{ marginBottom: '1rem' }}>
          Enter your new password below.
        </p>

        <form className="auth-form" onSubmit={handleSubmit}>
          <label>
            <span>New password</span>
            <input
              type="password"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              required
              minLength={6}
              autoComplete="new-password"
            />
          </label>
          <label>
            <span>Confirm new password</span>
            <input
              type="password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              required
              minLength={6}
              autoComplete="new-password"
            />
          </label>
          {error && <div className="form-error">{error}</div>}
          <button type="submit" className="btn btn-primary" disabled={loading}>
            {loading ? 'Updating...' : 'Change password'}
          </button>
        </form>
      </div>
    </div>
  )
}
