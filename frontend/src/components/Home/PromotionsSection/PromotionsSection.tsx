import { Tag } from 'lucide-react'
import { usePromotions } from '../../../hooks/features/usePromotions'
import styles from './PromotionsSection.module.css'

export default function PromotionsSection() {
  const { promotions } = usePromotions()

  if (promotions.length === 0) return null

  return (
    <section className={styles.section}>
      <h2 className={styles.title}>Special Offers</h2>
      <div className={styles.list}>
        {promotions.map(promo => (
          <div key={promo.id} className={styles.card}>
            <div className={styles.info}>
              <Tag size={20} />
              <div>
                <h3>{promo.title}</h3>
                <p>{promo.description || `-${promo.discountPercent}% off`}</p>
              </div>
            </div>
            <span className={styles.discount}>-{promo.discountPercent}%</span>
          </div>
        ))}
      </div>
    </section>
  )
}