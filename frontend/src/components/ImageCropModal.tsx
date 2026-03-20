import { useState, useCallback } from 'react'
import Cropper, { type Area } from 'react-easy-crop'
import { getCroppedImg } from '../utils/cropImage'

interface ImageCropModalProps {
  imageUrl: string
  fileName: string
  onComplete: (file: File) => void
  onCancel: () => void
}

const DEFAULT_ASPECT = 1 // Square crop for post images

export function ImageCropModal({ imageUrl, fileName, onComplete, onCancel }: ImageCropModalProps) {
  const [crop, setCrop] = useState({ x: 0, y: 0 })
  const [zoom, setZoom] = useState(1)
  const [croppedAreaPixels, setCroppedAreaPixels] = useState<Area | null>(null)
  const [saving, setSaving] = useState(false)

  const onCropComplete = useCallback((_: Area, croppedAreaPixels: Area) => {
    setCroppedAreaPixels(croppedAreaPixels)
  }, [])

  const handleSave = async () => {
    if (!croppedAreaPixels) return // onCropComplete sets this; usually available after first render
    setSaving(true)
    try {
      const croppedFile = await getCroppedImg(imageUrl, croppedAreaPixels, fileName)
      onComplete(croppedFile)
    } catch (err) {
      console.error('Failed to crop image:', err)
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="modal-overlay" onClick={onCancel}>
      <div
        className="modal-box image-crop-modal"
        onClick={(e) => e.stopPropagation()}
        style={{ maxWidth: 'min(500px, 96vw)' }}
      >
        <h3>Crop your image</h3>
        <p className="muted" style={{ marginBottom: '1rem', fontSize: '0.9rem' }}>
          Adjust the crop area, then click Apply.
        </p>
        <div className="crop-container" style={{ position: 'relative', height: 320, background: '#1a1a1a' }}>
          <Cropper
            image={imageUrl}
            crop={crop}
            zoom={zoom}
            aspect={DEFAULT_ASPECT}
            onCropChange={setCrop}
            onZoomChange={setZoom}
            onCropComplete={onCropComplete}
          />
        </div>
        <div style={{ marginTop: '1rem' }}>
          <label style={{ display: 'block', marginBottom: '0.25rem', fontSize: '0.85rem' }}>
            Zoom
          </label>
          <input
            type="range"
            min={1}
            max={3}
            step={0.1}
            value={zoom}
            onChange={(e) => setZoom(Number(e.target.value))}
            style={{ width: '100%' }}
          />
        </div>
        <div style={{ display: 'flex', gap: '0.75rem', marginTop: '1.25rem' }}>
          <button type="button" className="btn btn-secondary" onClick={onCancel} disabled={saving}>
            Cancel
          </button>
          <button type="button" className="btn btn-primary" onClick={handleSave} disabled={saving || !croppedAreaPixels}>
            {saving ? 'Applying...' : 'Apply'}
          </button>
        </div>
      </div>
    </div>
  )
}
