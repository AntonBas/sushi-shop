import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { Trash2 } from 'lucide-react'
import { useApi } from '../../../../hooks/common/useApi'
import { useNotification } from '../../../../context/NotificationContext'
import * as promotionsApi from '../../../../api/promotions'
import Button from '../../../../components/UI/Button/Button'
import Loading from '../../../../components/UI/Loading/Loading'
import Pagination from '../../../../components/UI/Pagination/Pagination'
import type { PromotionResponse } from '../../../../types'
import type { Page } from '../../../../types/common'
import styles from './AdminPromotionsPage.module.css'

export default function AdminPromotionsPage() {
  const navigate = useNavigate()
  const { showNotification } = useNotification()
  const { data, loading, execute } = useApi<Page<PromotionResponse>>()
  const [promotions, setPromotions] = useState<PromotionResponse[]>([])
  const [page, setPage] = useState(0)

  const loadPromotions = (p: number) => {
    execute(() => promotionsApi.getPromotions(p)).then((res) => setPromotions(res.content))
  }

  useEffect(() => {
    loadPromotions(page)
  }, [page])

  const handleDelete = async (id: number, title: string) => {
    if (!window.confirm(`Delete "${title}"?`)) return
    try {
      await promotionsApi.deletePromotion(id)
      showNotification('Promotion deleted', 'success')
      loadPromotions(page)
    } catch {
      showNotification('Failed to delete promotion', 'error')
    }
  }

  if (loading) return <Loading text="Loading promotions..." />

  return (
    <div className={styles.page}>
      <div className={styles.header}>
        <h1>Promotions</h1>
        <Button onClick={() => navigate('/admin/promotions/new')}>Add Promotion</Button>
      </div>

      {promotions.length === 0 ? (
        <div className={styles.empty}>
          <h3>No promotions found</h3>
          <p>Create your first promotion</p>
          <Button onClick={() => navigate('/admin/promotions/new')}>Add Promotion</Button>
        </div>
      ) : (
        <>
          <table className={styles.table}>
            <thead>
              <tr>
                <th>Title</th>
                <th>Discount</th>
                <th>Period</th>
                <th>Products</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {promotions.map((promo) => (
                <tr key={promo.id}>
                  <td className={styles.name}>{promo.title}</td>
                  <td>{promo.discountPercent}%</td>
                  <td>
                    {new Date(promo.startDate).toLocaleDateString()} – {new Date(promo.endDate).toLocaleDateString()}
                  </td>
                  <td>{promo.products.length}</td>
                  <td>
                    <span className={`${styles.statusBadge} ${promo.active ? styles.active : styles.inactive}`}>
                      {promo.active ? 'Active' : 'Inactive'}
                    </span>
                  </td>
                  <td>
                    <button onClick={() => handleDelete(promo.id, promo.title)} className={styles.deleteBtn}>
                      <Trash2 size={16} />
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          <Pagination
            currentPage={page}
            totalPages={data?.totalPages || 0}
            onPageChange={setPage}
          />
        </>
      )}
    </div>
  )
}