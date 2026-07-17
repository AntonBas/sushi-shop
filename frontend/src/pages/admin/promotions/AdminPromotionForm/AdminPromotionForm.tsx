import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { ArrowLeft } from 'lucide-react'
import { useApi } from '../../../../hooks/common/useApi'
import { useProducts } from '../../../../hooks/features/useProducts'
import { useNotification } from '../../../../context/NotificationContext'
import * as promotionsApi from '../../../../api/promotions'
import Button from '../../../../components/ui/Button/Button'
import Input from '../../../../components/ui/Input/Input'
import type { PromotionResponse } from '../../../../types'
import styles from './AdminPromotionForm.module.css'

export default function AdminPromotionForm() {
  const navigate = useNavigate()
  const { showNotification } = useNotification()
  const createApi = useApi<PromotionResponse>()
  const { products, loadProducts } = useProducts()

  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [discountPercent, setDiscountPercent] = useState('')
  const [startDate, setStartDate] = useState('')
  const [endDate, setEndDate] = useState('')
  const [selectedProductIds, setSelectedProductIds] = useState<number[]>([])

  useState(() => {
    loadProducts(0)
  })

  const toggleProduct = (id: number) => {
    setSelectedProductIds((prev) =>
      prev.includes(id) ? prev.filter((p) => p !== id) : [...prev, id]
    )
  }

  const handleSubmit = async (e: React.SyntheticEvent) => {
    e.preventDefault()
    try {
      await createApi.execute(() =>
        promotionsApi.createPromotion({
          title,
          description: description || undefined,
          discountPercent: Number(discountPercent),
          startDate: new Date(startDate).toISOString(),
          endDate: new Date(endDate).toISOString(),
          productIds: selectedProductIds,
        })
      )
      showNotification('Promotion created', 'success')
      navigate('/admin/promotions')
    } catch {
      showNotification('Failed to create promotion', 'error')
    }
  }

  return (
    <div className={styles.page}>
      <div className={styles.header}>
        <button onClick={() => navigate('/admin/promotions')} className={styles.backBtn}>
          <ArrowLeft size={20} />
        </button>
        <h1>New Promotion</h1>
      </div>

      <form onSubmit={handleSubmit} className={styles.form}>
        <Input label="Title" value={title} onChange={setTitle} placeholder="Weekend Sale" />

        <div className={styles.fieldGroup}>
          <label className={styles.label}>Description</label>
          <textarea
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            rows={3}
            className={styles.textarea}
            placeholder="Promotion description"
            maxLength={250}
          />
        </div>

        <Input label="Discount (%)" value={discountPercent} onChange={setDiscountPercent} placeholder="20" type="number" />

        <div className={styles.row}>
          <Input label="Start Date" value={startDate} onChange={setStartDate} type="datetime-local" />
          <Input label="End Date" value={endDate} onChange={setEndDate} type="datetime-local" />
        </div>

        <div className={styles.fieldGroup}>
          <label className={styles.label}>Products ({selectedProductIds.length} selected)</label>
          <div className={styles.productList}>
            {products.map((product) => (
              <button
                key={product.id}
                type="button"
                onClick={() => toggleProduct(product.id)}
                className={`${styles.productItem} ${selectedProductIds.includes(product.id) ? styles.selected : ''}`}
              >
                <span>{product.name}</span>
                <span className={styles.productPrice}>₴{product.price}</span>
              </button>
            ))}
          </div>
        </div>

        <div className={styles.actions}>
          <Button type="button" variant="secondary" onClick={() => navigate('/admin/promotions')}>
            Cancel
          </Button>
          <Button type="submit" loading={createApi.loading}>
            Create Promotion
          </Button>
        </div>
      </form>
    </div>
  )
}