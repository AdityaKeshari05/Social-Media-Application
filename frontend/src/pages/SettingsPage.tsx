import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../AuthContext'

export function SettingsPage() {
  const navigate = useNavigate()
  const { logout } = useAuth()
  const [showLogoutConfirm, setShowLogoutConfirm] = useState(false)

  return (
    <div className="page settings-page">
      <h1 className="page-title">Settings</h1>

      <div className="settings-menu">
        <Link to="/settings/personal-details" className="settings-menu-item">
          <div className="settings-menu-item-main">
            <div className="settings-menu-title">Personal details</div>
            <div className="settings-menu-subtitle">Username, email, and account info</div>
          </div>
          <div className="settings-menu-chevron" aria-hidden="true">
            ›
          </div>
        </Link>

        <Link to="/forgot-password" className="settings-menu-item">
          <div className="settings-menu-item-main">
            <div className="settings-menu-title">Forgot password</div>
            <div className="settings-menu-subtitle">Reset your password via email</div>
          </div>
          <div className="settings-menu-chevron" aria-hidden="true">
            ›
          </div>
        </Link>

        <button
          type="button"
          className="settings-menu-item settings-menu-item-button"
          onClick={() => {
            setShowLogoutConfirm(true)
          }}
        >
          <div className="settings-menu-item-main">
            <div className="settings-menu-title">Log out</div>
            <div className="settings-menu-subtitle">Sign out of this device</div>
          </div>
          <div className="settings-menu-chevron" aria-hidden="true">
            ›
          </div>
        </button>
      </div>

      {showLogoutConfirm && (
        <div
          className="modal-overlay"
          onClick={() => setShowLogoutConfirm(false)}
          role="presentation"
        >
          <div
            className="modal-box"
            onClick={(e) => e.stopPropagation()}
            role="dialog"
            aria-label="Log out confirmation"
          >
            <h3>Log out?</h3>
            <p className="muted">Are you sure you want to log out of your account?</p>
            <div className="modal-actions">
              <button
                type="button"
                className="btn btn-secondary"
                onClick={() => setShowLogoutConfirm(false)}
              >
                Cancel
              </button>
              <button
                type="button"
                className="btn btn-primary btn-danger"
                onClick={() => {
                  logout()
                  navigate('/login')
                  setShowLogoutConfirm(false)
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

