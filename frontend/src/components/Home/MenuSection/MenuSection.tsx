import { useState, useEffect } from 'react'
import { useProducts } from '../../../hooks/features/useProducts'
import { CATEGORY_DISPLAY } from '../../../types/enums'
import type { Category } from '../../../types'
import ProductCard from '../../Product/ProductCard/ProductCard'
import Pagination from '../../UI/Pagination/Pagination'
import Loading from '../../UI/Loading/Loading'
import { Search } from 'lucide-react'
import styles from './MenuSection.module.css'

export default function MenuSection() {
  const { products, totalPages, loading, loadProducts } = useProducts()
  const [page, setPage] = useState(0)
  const [search, setSearch] = useState('')
  const [activeCategory, setActiveCategory] = useState<Category | ''>('')

  const categories: Category[] = ['ROLL', 'SET', 'DRINK', 'DESSERT', 'SOUP', 'EXTRA']

  useEffect(() => {
    loadProducts(0, {})
  }, [])

  const handleSearch = () => {
    setPage(0)
    loadProducts(0, { search: search || undefined, category: activeCategory || undefined })
  }

  const handleCategoryChange = (cat: Category | '') => {
    setActiveCategory(cat)
    setPage(0)
    loadProducts(0, { search: search || undefined, category: cat || undefined })
  }

  return (
    <section className={styles.section}>
      <h2 className={styles.title}>Menu</h2>

      <div className={styles.filters}>
        <div className={styles.search}>
          <input
            type="text"
            value={search}
            onChange={e => setSearch(e.target.value)}
            placeholder="Search sushi..."
            className={styles.searchInput}
            onKeyDown={e => e.key === 'Enter' && handleSearch()}
          />
          <button onClick={handleSearch} className={styles.searchBtn}>
            <Search size={18} />
          </button>
        </div>

        <div className={styles.categories}>
          <button
            className={`${styles.categoryBtn} ${activeCategory === '' ? styles.active : ''}`}
            onClick={() => handleCategoryChange('')}
          >
            All
          </button>
          {categories.map(cat => (
            <button
              key={cat}
              className={`${styles.categoryBtn} ${activeCategory === cat ? styles.active : ''}`}
              onClick={() => handleCategoryChange(cat)}
            >
              {CATEGORY_DISPLAY[cat]}
            </button>
          ))}
        </div>
      </div>

      {loading ? (
        <Loading text="Loading menu..." />
      ) : products.length === 0 ? (
        <div className={styles.empty}>No products found</div>
      ) : (
        <>
          <div className={styles.grid}>
            {products.map(p => (
              <ProductCard key={p.id} product={p} />
            ))}
          </div>
          <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
        </>
      )}
    </section>
  )
}