import { useEffect, useState } from 'react'
import { useProducts } from '../../hooks/features/useProducts'
import ProductCard from '../../components/ProductCard/ProductCard'
import Pagination from '../../components/UI/Pagination/Pagination'
import Button from '../../components/UI/Button/Button'
import { Search } from 'lucide-react'
import styles from './Home.module.css'

export default function Home() {
  const { products, totalPages, loading, loadProducts } = useProducts()
  const [page, setPage] = useState(0)
  const [search, setSearch] = useState('')

  useEffect(() => {
    loadProducts({ page, search: search || undefined })
  }, [page])

  const handleSearch = () => {
    setPage(0)
    loadProducts({ page: 0, search: search || undefined })
  }

  return (
    <div className={styles.container}>
      <h1 className={styles.title}>Menu</h1>
      <div className={styles.search}>
        <input
          type="text"
          value={search}
          onChange={e => setSearch(e.target.value)}
          placeholder="Search sushi..."
          className={styles.searchInput}
          onKeyDown={e => e.key === 'Enter' && handleSearch()}
        />
        <Button onClick={handleSearch}>
          <Search size={18} />
          Search
        </Button>
      </div>

      {loading ? (
        <div className={styles.loading}>
          <div className={styles.spinner} />
        </div>
      ) : products.length === 0 ? (
        <div className={styles.empty}>
          <p>No products found</p>
        </div>
      ) : (
        <>
          <div className={styles.grid}>
            {products.map(p => (
              <ProductCard key={p.id} product={p} />
            ))}
          </div>
          <Pagination
            currentPage={page}
            totalPages={totalPages}
            onPageChange={setPage}
          />
        </>
      )}
    </div>
  )
}