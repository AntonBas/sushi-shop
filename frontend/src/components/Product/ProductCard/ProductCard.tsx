import { Link } from 'react-router-dom'
import { Star } from 'lucide-react'
import type { ProductListResponse } from '../../../types'
import styles from './ProductCard.module.css'

interface Props {
  product: ProductListResponse
}

export default function ProductCard({ product }: Props) {
  return (
    <Link to={`/products/${product.id}`} className={styles.card}>
      <div className={styles.image}>
        <img src={product.mainImage || '/placeholder.jpg'} alt={product.name} />
        {!product.available && <span className={styles.badge}>Unavailable</span>}
      </div>
      <div className={styles.info}>
        <h3>{product.name}</h3>
        <div className={styles.meta}>
          <span className={styles.category}>{product.category}</span>
          {product.weight && <span className={styles.weight}>{product.weight}g</span>}
          {product.pieces && <span className={styles.pieces}>{product.pieces} pcs</span>}
        </div>
        <div className={styles.priceRow}>
          {product.discountedPrice ? (
            <>
              <span className={styles.oldPrice}>₴{product.price}</span>
              <span className={styles.price}>₴{product.discountedPrice}</span>
            </>
          ) : (
            <span className={styles.price}>₴{product.price}</span>
          )}
        </div>
        {product.averageRating && (
          <span className={styles.rating}>
            <Star size={14} fill="#fbbf24" stroke="#fbbf24" />
            {product.averageRating.toFixed(1)}
          </span>
        )}
      </div>
    </Link>
  )
}