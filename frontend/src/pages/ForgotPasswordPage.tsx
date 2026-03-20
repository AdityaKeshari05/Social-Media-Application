import { useState } from 'react'
import type { FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { authApi } from '../api'

export function ForgotPasswordPage() {
  const [email, setEmail] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [success, setSuccess] = useState(false)

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault()
    setError(null)
    setLoading(true)
    try {
      await authApi.forgotPassword(email)
      setSuccess(true)
    } catch {
      setError('No account found with this email address.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="page auth-page">
      <div className="auth-card">
        <h1 className="page-title">Forgot password</h1>

        {success ? (
          <div className="auth-form">
            <div className="form-message">
              Check your inbox at <strong>{email}</strong>. We&apos;ve sent you a link to reset your password.
            </div>
            <Link to="/login" className="btn btn-primary" style={{ display: 'inline-block', textAlign: 'center', marginTop: '1rem' }}>
              Back to login
            </Link>
          </div>
        ) : (
          <form className="auth-form" onSubmit={handleSubmit}>
            <p className="muted" style={{ marginBottom: '1rem' }}>
              Enter the email address associated with your account. We&apos;ll send you a link to reset your password.
            </p>
            <label>
              <span>Email</span>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                autoComplete="email"
              />
            </label>
            {error && <div className="form-error">{error}</div>}
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? 'Sending...' : 'Send reset link'}
            </button>
            <Link to="/login" className="btn btn-ghost" style={{ marginTop: '0.5rem', display: 'inline-block' }}>
              Back to login
            </Link>
          </form>
        )}
      </div>
    </div>
  )
}
