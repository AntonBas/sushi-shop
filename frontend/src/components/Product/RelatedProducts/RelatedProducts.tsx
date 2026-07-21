import { useEffect } from 'react'
import { useProducts } from '../../../hooks/features/useProducts'
import ProductCard from '../ProductCard/ProductCard'
import styles from './RelatedProducts.module.css'

interface Props {
  productId: number
}

export default function RelatedProducts({ productId }: Props) {
  const { related, loadRelated, loading } = useProducts()

  useEffect(() => {
    loadRelated(productId)
  }, [productId])

  if (loading || related.length === 0) return null

  return (
    <div className={styles.section}>
      <h2 className={styles.title}>You May Also Like</h2>
      <div className={styles.grid}>
        {related.map((product) => (
          <ProductCard key={product.id} product={product} />
        ))}
      </div>
    </div>
  )
}