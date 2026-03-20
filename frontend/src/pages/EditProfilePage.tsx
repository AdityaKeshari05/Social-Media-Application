import { useEffect, useRef, useState } from 'react'
import type { FormEvent } from 'react'
import { Link } from 'react-router-dom'
import axios from 'axios'
import { profileApi } from '../api'
import type { ProfileDto } from '../api'
import { ImageCropModal } from '../components/ImageCropModal'

/** Ensure URL has a protocol so it's clickable; accept any pasted link (LinkedIn, etc.). */
function normalizeUrl(value: string): string {
  const trimmed = value.trim()
  if (!trimmed) return ''
  if (/^https?:\/\//i.test(trimmed)) return trimmed
  return 'https://' + trimmed
}

export function EditProfilePage() {
  const [profile, setProfile] = useState<ProfileDto | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const [nameValue, setNameValue] = useState('')
  const [link1Value, setLink1Value] = useState('')
  const [link2Value, setLink2Value] = useState('')
  const [bioValue, setBioValue] = useState('')

  const [saving, setSaving] = useState(false)
  const [saveError, setSaveError] = useState<string | null>(null)
  const [saveSuccess, setSaveSuccess] = useState<string | null>(null)

  const fileInputRef = useRef<HTMLInputElement>(null)

  const [cropImageUrl, setCropImageUrl] = useState<string | null>(null)
  const [cropFileName, setCropFileName] = useState<string>('')

  useEffect(() => {
    let active = true
    ;(async () => {
      try {
        const { data } = await profileApi.me()
        if (!active) return
        setProfile(data)
        setNameValue(data.name ?? '')
        setLink1Value(data.website ?? '')
        setLink2Value(data.link2 ?? '')
        setBioValue(data.bio ?? '')
      } catch (err) {
        console.error(err)
        if (!active) return
        setError('Failed to load profile')
      } finally {
        if (active) setLoading(false)
      }
    })()
    return () => { active = false }
  }, [])

  const normalizeProfile = (data: unknown): ProfileDto => {
    const d = data as any
    return {
      ...d,
      isPrivate: d?.isPrivate ?? d?.private ?? false,
      isFollowing: d?.isFollowing ?? d?.following ?? false,
      isFollowRequested: d?.isFollowRequested ?? d?.followRequested ?? d?.requestPending ?? false,
    } as ProfileDto
  }

  const showMessage = (success: string | null, err: string | null) => {
    setSaveSuccess(success)
    setSaveError(err)
    if (success) setTimeout(() => setSaveSuccess(null), 3000)
  }

  const handleSaveName = async (e: FormEvent) => {
    e.preventDefault()
    setSaving(true)
    setSaveError(null)
    setSaveSuccess(null)
    try {
      const { data } = await profileApi.updateName(nameValue.trim())
      setProfile(normalizeProfile(data))
      showMessage('Name updated', null)
    } catch (err) {
      showMessage(null, axios.isAxiosError(err) ? (err.response?.data as any)?.message ?? 'Could not update name' : 'Could not update name')
    } finally {
      setSaving(false)
    }
  }

  const handleSaveLinks = async (e: FormEvent) => {
    e.preventDefault()
    setSaving(true)
    setSaveError(null)
    setSaveSuccess(null)
    try {
      const link1 = link1Value.trim() ? normalizeUrl(link1Value) : ''
      const link2 = link2Value.trim() ? normalizeUrl(link2Value) : ''
      const { data } = await profileApi.updateWebsite(link1)
      setProfile(normalizeProfile(data))
      await profileApi.updateLink2(link2)
      const { data: fresh } = await profileApi.me()
      setProfile(normalizeProfile(fresh))
      setLink1Value(fresh.website ?? '')
      setLink2Value(fresh.link2 ?? '')
      showMessage('Links updated', null)
    } catch (err) {
      showMessage(null, axios.isAxiosError(err) ? (err.response?.data as any)?.message ?? 'Could not update links' : 'Could not update links')
    } finally {
      setSaving(false)
    }
  }

  const handleSaveBio = async (e: FormEvent) => {
    e.preventDefault()
    setSaving(true)
    setSaveError(null)
    setSaveSuccess(null)
    try {
      const { data } = await profileApi.updateBio(bioValue)
      setProfile(normalizeProfile(data))
      showMessage('Bio updated', null)
    } catch (err) {
      showMessage(null, axios.isAxiosError(err) ? (err.response?.data as any)?.message ?? 'Could not update bio' : 'Could not update bio')
    } finally {
      setSaving(false)
    }
  }

  const handleProfilePicture = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (!file) return

    setCropFileName(file.name)
    const reader = new FileReader()
    reader.addEventListener('load', () => setCropImageUrl(reader.result?.toString() || null))
    reader.readAsDataURL(file)

    e.target.value = ''
  }

  const handleCropComplete = async (croppedFile: File) => {
    setCropImageUrl(null)
    setSaving(true)
    setSaveError(null)
    setSaveSuccess(null)
    try {
      const { data } = await profileApi.updateProfilePicture(croppedFile)
      setProfile(normalizeProfile(data))
      showMessage('Profile picture updated', null)
    } catch {
      showMessage(null, 'Could not update profile picture')
    } finally {
      setSaving(false)
    }
  }

  const handleCropCancel = () => {
    setCropImageUrl(null)
  }

  if (loading) return <div className="page-centered">Loading…</div>
  if (error || !profile) return <div className="page-centered error">{error ?? 'Profile not found'}</div>

  return (
    <div className="page settings-page edit-profile-page">
      <div className="settings-subpage-top">
        <Link to="/me" className="btn btn-ghost">
          ← Back
        </Link>
        <h1 className="page-title">Edit profile</h1>
      </div>

      {/* Top box: profile picture + name on left, Change photo on right */}
      <div className="edit-profile-hero">
        <div className="edit-profile-hero-left">
          {profile.profilePicture ? (
            <img src={profile.profilePicture} alt="" className="edit-profile-avatar" />
          ) : (
            <div className="edit-profile-avatar edit-profile-avatar-placeholder" />
          )}
          <span className="edit-profile-hero-name">{profile.name?.trim() || profile.username}</span>
        </div>
        <div className="edit-profile-hero-right">
          <input
            ref={fileInputRef}
            type="file"
            accept="image/*"
            className="edit-profile-file-input"
            onChange={handleProfilePicture}
            disabled={saving}
            aria-label="Change profile photo"
          />
          <button
            type="button"
            className="edit-profile-change-photo-btn"
            onClick={() => fileInputRef.current?.click()}
            disabled={saving}
          >
            Change photo
          </button>
        </div>
      </div>

      {(saveSuccess || saveError) && (
        <div className="edit-profile-message">
          {saveSuccess && <span className="edit-profile-success">{saveSuccess}</span>}
          {saveError && <span className="form-error">{saveError}</span>}
        </div>
      )}

      {/* Update name */}
      <section className="settings-section edit-profile-section">
        <h2 className="settings-section-title">Name</h2>
        <form className="auth-form settings-form" onSubmit={handleSaveName}>
          <label>
            <span>Your name</span>
            <input
              type="text"
              value={nameValue}
              onChange={(e) => setNameValue(e.target.value)}
              placeholder="Name"
              maxLength={100}
            />
          </label>
          <button type="submit" className="btn btn-primary" disabled={saving}>
            {saving ? 'Saving…' : 'Save'}
          </button>
        </form>
      </section>

      {/* Links (max 2) - use text input so any URL (e.g. LinkedIn) is accepted */}
      <section className="settings-section edit-profile-section">
        <h2 className="settings-section-title">Links</h2>
        <p className="edit-profile-hint">You can add up to 2 links (e.g. LinkedIn, portfolio, website).</p>
        <form className="auth-form settings-form" onSubmit={handleSaveLinks}>
          <label>
            <span>Link 1</span>
            <input
              type="text"
              value={link1Value}
              onChange={(e) => setLink1Value(e.target.value)}
              placeholder="https://linkedin.com/in/yourprofile or any URL"
              autoComplete="url"
            />
          </label>
          <label>
            <span>Link 2</span>
            <input
              type="text"
              value={link2Value}
              onChange={(e) => setLink2Value(e.target.value)}
              placeholder="https://example.com or leave empty"
              autoComplete="url"
            />
          </label>
          <button type="submit" className="btn btn-primary" disabled={saving}>
            {saving ? 'Saving…' : 'Save links'}
          </button>
        </form>
      </section>

      {/* Bio */}
      <section className="settings-section edit-profile-section">
        <h2 className="settings-section-title">Bio</h2>
        <form className="auth-form settings-form" onSubmit={handleSaveBio}>
          <label>
            <span>About you</span>
            <textarea
              value={bioValue}
              onChange={(e) => setBioValue(e.target.value)}
              placeholder="Write something about yourself"
              rows={4}
            />
          </label>
          <button type="submit" className="btn btn-primary" disabled={saving}>
            {saving ? 'Saving…' : 'Save'}
          </button>
        </form>
      </section>

      {cropImageUrl && (
        <ImageCropModal
          imageUrl={cropImageUrl}
          fileName={cropFileName}
          onComplete={handleCropComplete}
          onCancel={handleCropCancel}
        />
      )}
    </div>
  )
}

