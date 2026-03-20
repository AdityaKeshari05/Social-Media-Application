import { type FormEvent, useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { postApi } from '../api'
import type { PostDto } from '../api'
import { ImageCropModal } from '../components/ImageCropModal'

export function EditPostPage() {
  const { id } = useParams<{ id: string }>()
  const postId = Number(id)
  const navigate = useNavigate()

  const [post, setPost] = useState<PostDto | null>(null)
  const [title, setTitle] = useState('')
  const [content, setContent] = useState('')
  const [imageFile, setImageFile] = useState<File | null>(null)
  const [imageToCrop, setImageToCrop] = useState<{ url: string; fileName: string } | null>(null)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let active = true
    ;(async () => {
      try {
        const { data } = await postApi.getById(postId)
        if (!active) return
        setPost(data)
        setTitle(data.title ?? '')
        setContent(data.content ?? '')
      } catch (err) {
        console.error(err)
        if (!active) return
        setError('Failed to load post')
      } finally {
        if (active) setLoading(false)
      }
    })()
    return () => {
      active = false
    }
  }, [postId])

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault()
    setError(null)
    setSaving(true)
    try {
      if (!post) {
        setError('Post data missing')
        return
      }
      await postApi.update(postId, { title, content, categoryId: post.categoryId })
      if (imageFile) {
        await postApi.updatePostImage(postId, imageFile)
      }
      navigate(`/posts/${postId}`)
    } catch (err) {
      console.error(err)
      setError('Could not update post')
    } finally {
      setSaving(false)
    }
  }

  if (loading) return <div className="page-centered">Loading...</div>
  if (error) return <div className="page-centered error">{error}</div>
  if (!post) return <div className="page-centered error">Post not found</div>

  return (
    <div className="page">
      <h1 className="page-title">Edit post</h1>
      <form className="auth-form" onSubmit={handleSubmit}>
        <label>
          <span>Title</span>
          <input value={title} onChange={(e) => setTitle(e.target.value)} required />
        </label>
        <label>
          <span>Content</span>
          <textarea value={content} onChange={(e) => setContent(e.target.value)} rows={4} required />
        </label>
        <label>
          <span>Replace image (optional)</span>
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
            New image selected (cropped). <button type="button" className="btn btn-ghost" style={{ padding: 0, marginLeft: '0.25rem' }} onClick={() => setImageFile(null)}>Remove</button>
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
        <button type="submit" className="btn btn-primary" disabled={saving}>
          {saving ? 'Saving...' : 'Save changes'}
        </button>
      </form>
    </div>
  )
}

