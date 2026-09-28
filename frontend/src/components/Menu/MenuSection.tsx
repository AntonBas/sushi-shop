import { useState, useEffect } from "react";
import { useProducts } from "../../hooks/features/useProducts";
import { pickAllowed, useDebouncedParamInput, useListSearchParams } from "../../hooks/common/useListSearchParams";
import { CATEGORIES, CATEGORY_DISPLAY } from "../../types/enums";
import type { Category } from "../../types";
import ProductCard from "../Product/ProductCard/ProductCard";
import Pagination from "../UI/Pagination/Pagination";
import Loading from "../UI/Loading/Loading";
import { Search } from "lucide-react";
import styles from "./MenuSection.module.css";

const SORT_OPTIONS = [
  { value: "popularity,desc", label: "Most Popular" },
  { value: "price,asc", label: "Price: Low to High" },
  { value: "price,desc", label: "Price: High to Low" },
  { value: "rating,desc", label: "Rating: High to Low" },
] as const;

const SORT_VALUES = SORT_OPTIONS.map((option) => option.value);

export default function MenuSection() {
  const { products, totalPages, loading, loadMoreProducts } = useProducts();
  const { getParam, updateParams } = useListSearchParams();
  const urlSearch = getParam("search");
  const activeCategory = pickAllowed(getParam("category"), CATEGORIES);
  const sort = pickAllowed(getParam("sort"), SORT_VALUES);
  const [search, handleSearchChange] = useDebouncedParamInput(urlSearch, (value) =>
    updateParams({ search: value }, { replace: true }),
  );

  const filtersKey = `${urlSearch}|${activeCategory}|${sort}`;
  const [page, setPage] = useState(0);
  const [pageFiltersKey, setPageFiltersKey] = useState(filtersKey);
  if (pageFiltersKey !== filtersKey) {
    setPageFiltersKey(filtersKey);
    setPage(0);
  }

  useEffect(() => {
    void loadMoreProducts(0, {
      search: urlSearch || undefined,
      category: activeCategory || undefined,
      sort: sort || undefined,
    });
  }, [loadMoreProducts, urlSearch, activeCategory, sort]);

  const handleCategoryChange = (cat: Category | "") => updateParams({ category: cat });

  const handleSortChange = (value: string) => updateParams({ sort: value });

  const handleLoadMore = () => {
    const nextPage = page + 1;
    if (nextPage >= totalPages) return;
    setPage(nextPage);
    void loadMoreProducts(nextPage, {
      search: urlSearch || undefined,
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
            aria-label="Search menu"
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
            {CATEGORIES.map((cat) => (
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
            {SORT_OPTIONS.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
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
