import { useEffect } from 'react'
import { useProducts } from '../../../hooks/features/useProducts'
import ProductCard from '../../Product/ProductCard/ProductCard'
import styles from './PopularSection.module.css'

export default function PopularSection() {
  const { popular, loadPopular } = useProducts()

  useEffect(() => {
    loadPopular()
  }, [loadPopular])

  if (popular.length === 0) return null

  return (
    <section className={styles.section}>
      <h2 className={styles.title}>Popular Now</h2>
      <div className={styles.grid}>
        {popular.map(p => (
          <ProductCard key={p.id} product={p} />
        ))}
      </div>
    </section>
  )
}