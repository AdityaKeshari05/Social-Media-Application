import { type FormEvent, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../AuthContext'

export function LoginPage() {
  const navigate = useNavigate()
  const { loginStepOne, loginStepTwo, emailForOtp } = useAuth()

  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [otp, setOtp] = useState('')
  const [step, setStep] = useState<'credentials' | 'otp'>('credentials')
  const [message, setMessage] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  const handleCredentialsSubmit = async (e: FormEvent) => {
    e.preventDefault()
    setError(null)
    setLoading(true)
    try {
      const data = await loginStepOne(username, password)
      if (data) {
        setMessage(data.message)
        setStep('otp')
      }
    } catch {
      setError('Invalid username or password')
    } finally {
      setLoading(false)
    }
  }

  const handleOtpSubmit = async (e: FormEvent) => {
    e.preventDefault()
    setError(null)
    setLoading(true)
    try {
      const ok = await loginStepTwo(otp)
      if (ok) {
        navigate('/')
      }
    } catch {
      setError('Invalid OTP')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="page auth-page">
      <div className="auth-card">
        <h1 className="page-title">Log in</h1>

        {step === 'credentials' && (
          <>
            <form className="auth-form" onSubmit={handleCredentialsSubmit}>
            <label>
              <span>Username</span>
              <input
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                autoComplete="username"
                required
              />
            </label>
            <label>
              <span>Password</span>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                autoComplete="current-password"
                required
              />
            </label>
            <Link to="/forgot-password" className="muted" style={{ fontSize: '0.85rem', marginTop: '-0.5rem', marginBottom: '0.5rem', display: 'block' }}>
              Forgot password?
            </Link>
            {error && <div className="form-error">{error}</div>}
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? 'Sending OTP...' : 'Continue'}
            </button>
            <div className="auth-divider">
              <span>or</span>
            </div>
            <a className="btn btn-google" href="http://localhost:8080/oauth2/authorization/google">
              <svg className="btn-google-icon" width="18" height="18" viewBox="0 0 18 18" aria-hidden="true">
                <path fill="#4285F4" d="M16.51 8H8.98v3h4.3c-.18 1-.74 1.48-1.6 2.04v2.01h2.6a7.8 7.8 0 0 0 2.38-5.88c0-.57-.05-.66-.15-1.18z" />
                <path fill="#34A853" d="M8.98 17c2.16 0 3.97-.72 5.3-1.94l-2.6-2a4.8 4.8 0 0 1-7.18-2.54H1.83v2.07A8 8 0 0 0 8.98 17z" />
                <path fill="#FBBC05" d="M4.5 10.52a4.8 4.8 0 0 1 0-3.04V5.41H1.83a8 8 0 0 0 0 7.18l2.67-2.07z" />
                <path fill="#EA4335" d="M8.98 4.18c1.17 0 2.23.4 3.06 1.2l2.3-2.3A8 8 0 0 0 1.83 5.4L4.5 7.49a4.77 4.77 0 0 1 4.48-3.3z" />
              </svg>
              Continue with Google
            </a>
            </form>
          </>
        )}

        {step === 'otp' && (
          <form className="auth-form" onSubmit={handleOtpSubmit}>
            {message && <div className="form-message">{message}</div>}
            {emailForOtp && (
              <div className="muted">We sent a 6‑digit code to {emailForOtp}.</div>
            )}
            <label>
              <span>One‑time code</span>
              <input
                value={otp}
                onChange={(e) => setOtp(e.target.value)}
                inputMode="numeric"
                maxLength={6}
                required
              />
            </label>
            {error && <div className="form-error">{error}</div>}
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? 'Verifying...' : 'Log in'}
            </button>
          </form>
        )}
      </div>
    </div>
  )
}

