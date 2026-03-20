import { type FormEvent, useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { categoryApi, postApi } from '../api'
import type { Category } from '../api'
import { ImageCropModal } from '../components/ImageCropModal'

export function CreatePostPage() {
  const navigate = useNavigate()
  const [title, setTitle] = useState('')
  const [content, setContent] = useState('')
  const [categoryId, setCategoryId] = useState<number | ''>('')
  const [imageFile, setImageFile] = useState<File | null>(null)
  const [imageToCrop, setImageToCrop] = useState<{ url: string; fileName: string } | null>(null)
  const [categories, setCategories] = useState<Category[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let active = true
    ;(async () => {
      try {
        const { data } = await categoryApi.getAll()
        if (!active) return
        setCategories(data)
      } catch (err) {
        console.error(err)
      }
    })()
    return () => {
      active = false
    }
  }, [])

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault()
    if (!categoryId) {
      setError('Please select a category')
      return
    }
    setError(null)
    setLoading(true)
    try {
      const { data: created } = await postApi.create({
        title,
        content,
        categoryId: Number(categoryId),
      })

      if (imageFile) {
        try {
          await postApi.updatePostImage(created.id, imageFile)
        } catch (uploadErr) {
          console.error('Failed to upload post image:', uploadErr)
          setError('Post created but image upload failed')
        }
      }

      navigate(`/posts/${created.id}`)
    } catch (err) {
      console.error(err)
      setError('Could not create post')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="page">
      <h1 className="page-title">Create post</h1>
      <form className="auth-form" onSubmit={handleSubmit}>
        <label>
          <span>Title</span>
          <input value={title} onChange={(e) => setTitle(e.target.value)} required />
        </label>
        <label>
          <span>Content</span>
          <textarea
            value={content}
            onChange={(e) => setContent(e.target.value)}
            rows={4}
            required
          />
        </label>
        <label>
          <span>Category</span>
          <select
            value={categoryId}
            onChange={(e) => setCategoryId(e.target.value ? Number(e.target.value) : '')}
            required
          >
            <option value="">Select a category</option>
            {categories.map((c) => (
              <option key={c.id} value={c.id}>
                {c.name}
              </option>
            ))}
          </select>
        </label>
        <label>
          <span>Image (optional)</span>
          <input
            type="file"
            accept="image/*"
            onChange={(e) => {
              const file = e.target.files?.[0]
              if (file) {
                setImageToCrop({ url: URL.createObjectURL(file), fileName: file.name })
              }
              e.target.value = ''
            }}
          />
        </label>
        {imageFile && (
          <div className="muted" style={{ fontSize: '0.85rem' }}>
            Image selected (cropped). <button type="button" className="btn btn-ghost" style={{ padding: 0, marginLeft: '0.25rem' }} onClick={() => setImageFile(null)}>Remove</button>
          </div>
        )}
        {imageToCrop && (
          <ImageCropModal
            imageUrl={imageToCrop.url}
            fileName={imageToCrop.fileName}
            onComplete={(file) => {
              setImageFile(file)
              URL.revokeObjectURL(imageToCrop.url)
              setImageToCrop(null)
            }}
            onCancel={() => {
              URL.revokeObjectURL(imageToCrop.url)
              setImageToCrop(null)
            }}
          />
        )}
        {error && <div className="form-error">{error}</div>}
        <button type="submit" className="btn btn-primary" disabled={loading}>
          {loading ? 'Publishing...' : 'Publish'}
        </button>
      </form>
    </div>
  )
}

