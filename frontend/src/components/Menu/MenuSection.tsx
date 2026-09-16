import { useState, useEffect, useRef } from "react";
import { useSearchParams } from "react-router-dom";
import { useProducts } from "../../hooks/features/useProducts";
import { CATEGORY_DISPLAY } from "../../types/enums";
import type { Category } from "../../types";
import ProductCard from "../Product/ProductCard/ProductCard";
import Pagination from "../UI/Pagination/Pagination";
import Loading from "../UI/Loading/Loading";
import { Search } from "lucide-react";
import styles from "./MenuSection.module.css";

export default function MenuSection() {
  const { products, totalPages, loading, loadMoreProducts } = useProducts();
  const [searchParams, setSearchParams] = useSearchParams();
  const [page, setPage] = useState(0);
  const categories: Category[] = Object.keys(CATEGORY_DISPLAY) as Category[];

  const [search, setSearch] = useState(() => searchParams.get("search") || "");
  const [activeCategory, setActiveCategory] = useState<Category | "">(() => {
    const cat = searchParams.get("category") as Category | "";
    return cat && categories.includes(cat) ? cat : "";
  });
  const [sort, setSort] = useState(() => searchParams.get("sort") || "");
  const debounceRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  useEffect(() => {
    loadMoreProducts(0, {
      search: search || undefined,
      category: activeCategory || undefined,
      sort: sort || undefined,
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [loadMoreProducts]);

  const buildSearchParams = (overrides: { search?: string; category?: Category | ""; sort?: string }) => {
    const params: Record<string, string> = {};
    if (overrides.search) params.search = overrides.search;
    if (overrides.category) params.category = overrides.category;
    if (overrides.sort) params.sort = overrides.sort;
    return params;
  };

  const handleSearchChange = (value: string) => {
    setSearch(value);
    if (debounceRef.current) clearTimeout(debounceRef.current);
    debounceRef.current = setTimeout(() => {
      setPage(0);
      setSearchParams(buildSearchParams({ search: value, category: activeCategory, sort }));
      loadMoreProducts(0, {
        search: value || undefined,
        category: activeCategory || undefined,
        sort: sort || undefined,
      });
    }, 300);
  };

  const handleCategoryChange = (cat: Category | "") => {
    setActiveCategory(cat);
    setPage(0);
    setSearchParams(buildSearchParams({ search, category: cat, sort }));
    loadMoreProducts(0, {
      search: search || undefined,
      category: cat || undefined,
      sort: sort || undefined,
    });
  };

  const handleSortChange = (value: string) => {
    setSort(value);
    setPage(0);
    setSearchParams(buildSearchParams({ search, category: activeCategory, sort: value }));
    loadMoreProducts(0, {
      search: search || undefined,
      category: activeCategory || undefined,
      sort: value || undefined,
    });
  };

  const handleLoadMore = () => {
    const nextPage = page + 1;
    if (nextPage >= totalPages) return;
    setPage(nextPage);
    loadMoreProducts(nextPage, {
      search: search || undefined,
      category: activeCategory || undefined,
      sort: sort || undefined,
    });
  };

  return (
    <section className={styles.section}>
      <h2 className={styles.title}>Menu</h2>

      <div className={styles.filters}>
        <div className={styles.search}>
          <Search size={18} className={styles.searchIcon} />
          <input
            type="text"
            value={search}
            onChange={(e) => handleSearchChange(e.target.value)}
            placeholder="Search..."
            className={styles.searchInput}
          />
        </div>

        <div className={styles.sortRow}>
          <div className={styles.categories}>
            <button
              className={`${styles.categoryBtn} ${activeCategory === "" ? styles.active : ""}`}
              onClick={() => handleCategoryChange("")}
            >
              All
            </button>
            {categories.map((cat) => (
              <button
                key={cat}
                className={`${styles.categoryBtn} ${activeCategory === cat ? styles.active : ""}`}
                onClick={() => handleCategoryChange(cat)}
              >
                {CATEGORY_DISPLAY[cat]}
              </button>
            ))}
          </div>

          <select
            value={sort}
            onChange={(e) => handleSortChange(e.target.value)}
            className={styles.sortSelect}
            aria-label="Sort products"
          >
            <option value="">Sort: Default</option>
            <option value="price,asc">Price: Low to High</option>
            <option value="price,desc">Price: High to Low</option>
            <option value="rating,desc">Rating: High to Low</option>
          </select>
        </div>
      </div>

      {loading && page === 0 ? (
        <Loading text="Loading menu..." />
      ) : products.length === 0 ? (
        <div className={styles.empty}>No products found</div>
      ) : (
        <>
          <div className={styles.grid}>
            {products.map((p) => (
              <ProductCard key={p.id} product={p} />
            ))}
          </div>
          {page < totalPages - 1 && (
            <Pagination
              currentPage={page}
              totalPages={totalPages}
              onPageChange={handleLoadMore}
              variant="load-more"
              loading={loading}
            />
          )}
        </>
      )}
    </section>
  );
}
