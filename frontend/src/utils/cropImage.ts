import type { Area } from 'react-easy-crop'

export function createImage(url: string): Promise<HTMLImageElement> {
  return new Promise((resolve, reject) => {
    const image = new Image()
    image.addEventListener('load', () => resolve(image))
    image.addEventListener('error', reject)
    image.setAttribute('crossOrigin', 'anonymous')
    image.src = url
  })
}

export async function getCroppedImg(
  imageSrc: string,
  pixelCrop: Area,
  fileName: string
): Promise<File> {
  const image = await createImage(imageSrc)
  const canvas = document.createElement('canvas')
  canvas.width = pixelCrop.width
  canvas.height = pixelCrop.height
  const ctx = canvas.getContext('2d')
  if (!ctx) throw new Error('No 2d context')

  ctx.drawImage(
    image,
    pixelCrop.x,
    pixelCrop.y,
    pixelCrop.width,
    pixelCrop.height,
    0,
    0,
    pixelCrop.width,
    pixelCrop.height
  )

  return new Promise((resolve, reject) => {
    canvas.toBlob((blob) => {
      if (!blob) {
        reject(new Error('Canvas is empty'))
        return
      }
      const baseName = fileName.replace(/\.[^.]+$/, '') || 'image'
      const outputName = `${baseName}_cropped.jpg`
      const file = new File([blob], outputName, { type: 'image/jpeg' })
      resolve(file)
    }, 'image/jpeg', 0.92)
  })
}
