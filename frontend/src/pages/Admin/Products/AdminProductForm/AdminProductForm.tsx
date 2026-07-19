import { useState, useEffect, useRef } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { ArrowLeft, Upload, X } from 'lucide-react'
import { useProducts } from '../../../../hooks/features/useProducts'
import { useNotification } from '../../../../context/NotificationContext'
import * as productsApi from '../../../../api/products'
import Button from '../../../../components/ui/Button/Button'
import Input from '../../../../components/ui/Input/Input'
import Loading from '../../../../components/ui/Loading/Loading'
import type { Category } from '../../../../types'
import styles from './AdminProductForm.module.css'

export default function AdminProductForm() {
  const { id } = useParams<{ id: string }>()
  const isEdit = !!id
  const navigate = useNavigate()
  const { product, productLoading, getProduct } = useProducts()
  const { showNotification } = useNotification()
  const fileInputRef = useRef<HTMLInputElement>(null)

  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const [price, setPrice] = useState('')
  const [category, setCategory] = useState<Category>('ROLL')
  const [images, setImages] = useState<File[]>([])
  const [existingImages, setExistingImages] = useState<{ id: number; url: string }[]>([])
  const [isSubmitting, setIsSubmitting] = useState(false)

  useEffect(() => {
    if (isEdit && id) {
      getProduct(Number(id))
    }
  }, [isEdit, id])

  useEffect(() => {
    if (isEdit && product) {
      setName(product.name)
      setDescription(product.description || '')
      setPrice(product.price.toString())
      setCategory(product.category as Category)
      setExistingImages(product.images.map((url, index) => ({ id: index, url })))
    }
  }, [isEdit, product])

  const handleImageAdd = (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = e.target.files
    if (files) setImages((prev) => [...prev, ...Array.from(files)])
  }

  const handleRemoveNewImage = (index: number) => {
    setImages((prev) => prev.filter((_, i) => i !== index))
  }

  const handleRemoveExistingImage = async (imageUrl: string) => {
    if (isEdit && id) {
      try {
        const imageId = existingImages.find((img) => img.url === imageUrl)?.id
        if (imageId !== undefined) {
          await productsApi.deleteProductImage(Number(id), imageId)
        }
        setExistingImages((prev) => prev.filter((img) => img.url !== imageUrl))
        showNotification('Image removed', 'success')
      } catch {
        showNotification('Failed to remove image', 'error')
      }
    } else {
      setExistingImages((prev) => prev.filter((img) => img.url !== imageUrl))
    }
  }

  const handleSubmit = async (e: React.SyntheticEvent) => {
    e.preventDefault()
    setIsSubmitting(true)
    try {
      if (isEdit && id) {
        await productsApi.updateProduct(Number(id), {
          name: name || undefined,
          description: description || undefined,
          price: price ? Number(price) : undefined,
          category: category || undefined,
        })
        if (images.length > 0) {
          for (const image of images) {
            await productsApi.addProductImage(Number(id), image)
          }
        }
        showNotification('Product updated', 'success')
      } else {
        await productsApi.createProduct(
          {
            name,
            description: description || undefined,
            price: Number(price),
            category,
          },
          images.length > 0 ? images : undefined
        )
        showNotification('Product created', 'success')
      }
      navigate('/admin/products')
    } catch {
      showNotification('Failed to save product', 'error')
    } finally {
      setIsSubmitting(false)
    }
  }

  const categories: Category[] = ['ROLL', 'SET', 'DRINK', 'DESSERT', 'SOUP', 'EXTRA']

  if (isEdit && productLoading) return <Loading text="Loading product..." />

  return (
    <div className={styles.page}>
      <div className={styles.header}>
        <button onClick={() => navigate('/admin/products')} className={styles.backBtn}>
          <ArrowLeft size={20} />
        </button>
        <h1>{isEdit ? 'Edit Product' : 'New Product'}</h1>
      </div>

      <form onSubmit={handleSubmit} className={styles.form}>
        <div className={styles.layout}>
          <div className={styles.imagesSection}>
            <label className={styles.label}>Images</label>
            <div className={styles.imageGrid}>
              {existingImages.map((img) => (
                <div key={img.url} className={styles.imageItem}>
                  <img src={img.url} alt="" />
                  <button type="button" onClick={() => handleRemoveExistingImage(img.url)} className={styles.removeBtn}>
                    <X size={14} />
                  </button>
                </div>
              ))}
              {images.map((file, index) => (
                <div key={index} className={styles.imageItem}>
                  <img src={URL.createObjectURL(file)} alt="" />
                  <button type="button" onClick={() => handleRemoveNewImage(index)} className={styles.removeBtn}>
                    <X size={14} />
                  </button>
                </div>
              ))}
              <button type="button" onClick={() => fileInputRef.current?.click()} className={styles.addImage}>
                <Upload size={20} />
                <span>Add</span>
              </button>
            </div>
            <input
              ref={fileInputRef}
              type="file"
              accept="image/*"
              multiple
              onChange={handleImageAdd}
              hidden
            />
          </div>

          <div className={styles.fields}>
            <Input label="Name" value={name} onChange={setName} placeholder="Product name" />

            <div className={styles.fieldGroup}>
              <label className={styles.label}>Description</label>
              <textarea
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                rows={3}
                className={styles.textarea}
                placeholder="Product description"
                maxLength={250}
              />
            </div>

            <Input label="Price (₴)" value={price} onChange={setPrice} placeholder="250.00" type="number" />

            <div className={styles.fieldGroup}>
              <label className={styles.label}>Category</label>
              <select
                value={category}
                onChange={(e) => setCategory(e.target.value as Category)}
                className={styles.select}
              >
                {categories.map((c) => (
                  <option key={c} value={c}>{c}</option>
                ))}
              </select>
            </div>
          </div>
        </div>

        <div className={styles.actions}>
          <Button type="button" variant="secondary" onClick={() => navigate('/admin/products')}>
            Cancel
          </Button>
          <Button type="submit" loading={isSubmitting}>
            {isEdit ? 'Update Product' : 'Create Product'}
          </Button>
        </div>
      </form>
    </div>
  )
}