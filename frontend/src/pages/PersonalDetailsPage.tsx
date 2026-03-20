import { useEffect, useMemo, useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import axios from 'axios'
import { profileApi, userApi, accountApi } from '../api'
import type { UserDto } from '../api'
import { useAuth } from '../AuthContext'

type Availability = 'idle' | 'checking' | 'available' | 'taken' | 'invalid'

const USERNAME_REGEX = /^[a-zA-Z0-9_.]{3,30}$/

export function PersonalDetailsPage() {
  const navigate = useNavigate()
  const { logout } = useAuth()
  const [user, setUser] = useState<UserDto | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const [newUsername, setNewUsername] = useState('')
  const [availability, setAvailability] = useState<Availability>('idle')
  const [availabilityMsg, setAvailabilityMsg] = useState<string | null>(null)
  const [saveOpen, setSaveOpen] = useState(false)
  const [saving, setSaving] = useState(false)
  const [saveError, setSaveError] = useState<string | null>(null)
  const [showLogoutConfirm, setShowLogoutConfirm] = useState(false)
  const [togglingPrivacy, setTogglingPrivacy] = useState(false)
  const [privacyConfirm, setPrivacyConfirm] = useState<'private' | 'public' | null>(null)

  useEffect(() => {
    let active = true
    ;(async () => {
      try {
        const { data: profile } = await profileApi.me()
        if (!active) return
        if (!profile.userId) throw new Error('Missing user id')
        const { data: userData } = await userApi.getById(profile.userId)
        if (!active) return
        setUser(userData)
        setNewUsername(userData.username)
      } catch (err) {
        console.error(err)
        if (!active) return
        setError('Failed to load personal details')
      } finally {
        if (active) setLoading(false)
      }
    })()
    return () => {
      active = false
    }
  }, [])

  const normalized = useMemo(() => newUsername.trim(), [newUsername])

  useEffect(() => {
    if (!user) return
    const value = normalized

    if (!value || value === user.username) {
      setAvailability('idle')
      setAvailabilityMsg(null)
      return
    }

    if (!USERNAME_REGEX.test(value)) {
      setAvailability('invalid')
      setAvailabilityMsg('3–30 chars: letters, numbers, underscore, dot')
      return
    }

    let cancelled = false
    setAvailability('checking')
    setAvailabilityMsg('Checking…')
    const t = window.setTimeout(async () => {
      try {
        const req = userApi.usernameAvailable(value)
        const timeout = new Promise<never>((_, reject) =>
          window.setTimeout(() => reject(new Error('timeout')), 4000)
        )
        const { data } = await Promise.race([req, timeout])
        if (cancelled) return
        if ((data as any).available) {
          setAvailability('available')
          setAvailabilityMsg('Available')
        } else {
          setAvailability('taken')
          setAvailabilityMsg('Already taken')
        }
      } catch (err) {
        console.error(err)
        if (cancelled) return
        setAvailability('invalid')
        setAvailabilityMsg('Could not check')
      }
    }, 350)

    return () => {
      cancelled = true
      window.clearTimeout(t)
    }
  }, [normalized, user])

  const canSave = !!user && availability === 'available'

  const handleSave = (e: FormEvent) => {
    e.preventDefault()
    if (!canSave) return
    setSaveOpen(true)
    setSaveError(null)
  }

  const performSave = async () => {
    if (!user) return
    setSaving(true)
    setSaveError(null)
    try {
      const { data: profile } = await profileApi.me()
      if (!profile.userId) throw new Error('Missing user id')
      const { data: updated } = await userApi.update(profile.userId, { username: normalized })
      setUser(updated)
      setNewUsername(updated.username)
      setAvailability('idle')
      setAvailabilityMsg(null)
      setSaveOpen(false)
      setShowLogoutConfirm(true)
    } catch (err) {
      const msg = axios.isAxiosError(err)
        ? (err.response?.data as any)?.message ?? err.message
        : 'Could not update username'
      setSaveError(msg)
    } finally {
      setSaving(false)
    }
  }

  const handleTogglePrivacy = () => {
    if (!user || togglingPrivacy) return
    if (user.accountVisibility === 'PUBLIC') {
      setPrivacyConfirm('private')
    } else {
      setPrivacyConfirm('public')
    }
  }

  if (loading) return <div className="page-centered">Loading…</div>
  if (error || !user) return <div className="page-centered error">{error ?? 'Failed to load'}</div>

  return (
    <div className="page settings-page">
      <div className="settings-subpage-top">
        <Link to="/settings" className="btn btn-ghost">
          ← Back
        </Link>
        <h1 className="page-title">Personal details</h1>
      </div>

      <section className="settings-section">
        <h2 className="settings-section-title">Account</h2>
        <dl className="settings-details">
          <div>
            <dt>Username</dt>
            <dd>{user.username}</dd>
          </div>
          <div>
            <dt>Email</dt>
            <dd>{user.email}</dd>
          </div>
        </dl>
      </section>

      <section className="settings-section">
        <h2 className="settings-section-title">Account Privacy</h2>
        <div className="privacy-card">
          <div className="privacy-info">
            <div className="privacy-header">
              <span className="privacy-icon">
                {user.accountVisibility === 'PRIVATE' ? '🔒' : '🌍'}
              </span>
              <h3>
                {user.accountVisibility === 'PRIVATE' ? 'Private Account' : 'Public Account'}
              </h3>
            </div>
            <p className="privacy-description">
              {user.accountVisibility === 'PRIVATE'
                ? 'Only followers you approve can see what you share, including your photos and videos.'
                : 'Anyone on or off the application can see your posts and profile. Choose this to grow your audience.'}
            </p>
          </div>
          <div className="privacy-action">
            <label className="switch">
              <input
                type="checkbox"
                checked={user.accountVisibility === 'PRIVATE'}
                onChange={handleTogglePrivacy}
                disabled={togglingPrivacy}
              />
              <span className="slider round"></span>
            </label>
            {togglingPrivacy && <span className="updating-text">Updating...</span>}
          </div>
        </div>
      </section>

      <section className="settings-section">
        <h2 className="settings-section-title">Change username</h2>
        <form className="auth-form settings-form" onSubmit={handleSave}>
          <label className="settings-username-row">
            <span>New username</span>
            <div className="settings-username-input-wrap">
              <input
                type="text"
                value={newUsername}
                onChange={(e) => setNewUsername(e.target.value)}
                placeholder="Username"
                autoComplete="username"
              />
              {availability !== 'idle' && (
                <span className={`username-status username-status-${availability}`}>
                  {availabilityMsg}
                </span>
              )}
            </div>
          </label>

          <button type="submit" className="btn btn-primary" disabled={!canSave}>
            Save username
          </button>
          {!canSave && normalized && normalized !== user.username && (
            <div className="muted" style={{ marginTop: 8 }}>
              Pick an available username to enable saving.
            </div>
          )}
        </form>
      </section>

      {saveOpen && (
        <div className="modal-overlay" onClick={() => !saving && setSaveOpen(false)} role="presentation">
          <div
            className="modal-box"
            onClick={(e) => e.stopPropagation()}
            role="dialog"
            aria-label="Confirm username change"
          >
            <h3>Change username and log out?</h3>
            <p className="muted">
              To fully apply this change, you’ll need to log out and log back in. Change your username to{' '}
              <strong>{normalized}</strong> now?
            </p>
            {saveError && <div className="form-error">{saveError}</div>}
            <div className="modal-actions">
              <button
                type="button"
                className="btn btn-secondary"
                onClick={() => setSaveOpen(false)}
                disabled={saving}
              >
                Cancel
              </button>
              <button
                type="button"
                className="btn btn-primary"
                onClick={performSave}
                disabled={saving}
              >
                {saving ? 'Saving…' : 'Confirm'}
              </button>
            </div>
          </div>
        </div>
      )}
      {privacyConfirm && (
        <div className="modal-overlay" onClick={() => !togglingPrivacy && setPrivacyConfirm(null)} role="presentation">
          <div
            className="modal-box"
            onClick={(e) => e.stopPropagation()}
            role="dialog"
            aria-label="Account privacy confirmation"
          >
            <h3>{privacyConfirm === 'private' ? 'Switch to private account?' : 'Switch to public account?'}</h3>
            <p className="muted">
              {privacyConfirm === 'private'
                ? 'When your account is private, only people you approve will be able to see your posts and followers.'
                : 'When your account is public, anyone can see your posts and followers.'}
            </p>
            <div className="modal-actions">
              <button
                type="button"
                className="btn btn-secondary"
                onClick={() => setPrivacyConfirm(null)}
                disabled={togglingPrivacy}
              >
                Cancel
              </button>
              <button
                type="button"
                className="btn btn-primary"
                onClick={async () => {
                  if (!user) return
                  setTogglingPrivacy(true)
                  try {
                    if (privacyConfirm === 'private') {
                      await accountApi.makePrivate()
                      setUser({ ...user, accountVisibility: 'PRIVATE' })
                    } else {
                      await accountApi.makePublic()
                      setUser({ ...user, accountVisibility: 'PUBLIC' })
                    }
                    setPrivacyConfirm(null)
                  } catch (err) {
                    console.error('Failed to toggle privacy', err)
                    alert('Failed to change account privacy.')
                  } finally {
                    setTogglingPrivacy(false)
                  }
                }}
                disabled={togglingPrivacy}
              >
                {togglingPrivacy ? 'Updating…' : 'Confirm'}
              </button>
            </div>
          </div>
        </div>
      )}
      {showLogoutConfirm && (
        <div className="modal-overlay" onClick={() => setShowLogoutConfirm(false)} role="presentation">
          <div
            className="modal-box"
            onClick={(e) => e.stopPropagation()}
            role="dialog"
            aria-label="Log out confirmation"
          >
            <h3>Log out now?</h3>
            <p className="muted">You’ll need to log back in with your updated username.</p>
            <div className="modal-actions">
              <button
                type="button"
                className="btn btn-secondary"
                onClick={() => setShowLogoutConfirm(false)}
              >
                Stay logged in
              </button>
              <button
                type="button"
                className="btn btn-primary btn-danger"
                onClick={() => {
                  logout()
                  navigate('/login')
                }}
              >
                Log out
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

