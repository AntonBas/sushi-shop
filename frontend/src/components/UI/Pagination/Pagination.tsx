import styles from "./Pagination.module.css";

interface Props {
  currentPage: number;
  totalPages: number;
  onPageChange: (page: number) => void;
  variant?: "pages" | "load-more";
  loading?: boolean;
}

const MAX_VISIBLE_PAGES = 5;

function getVisiblePages(currentPage: number, totalPages: number): number[] {
  if (totalPages <= MAX_VISIBLE_PAGES) {
    return Array.from({ length: totalPages }, (_, i) => i);
  }

  let start = Math.max(0, currentPage - Math.floor(MAX_VISIBLE_PAGES / 2));
  let end = start + MAX_VISIBLE_PAGES;

  if (end > totalPages) {
    end = totalPages;
    start = totalPages - MAX_VISIBLE_PAGES;
  }

  return Array.from({ length: end - start }, (_, i) => start + i);
}

export default function Pagination({
  currentPage,
  totalPages,
  onPageChange,
  variant = "pages",
  loading,
}: Props) {
  if (totalPages <= 1) return null;

  if (variant === "load-more") {
    return (
      <button
        type="button"
        className={styles.loadMore}
        onClick={() => onPageChange(currentPage + 1)}
        disabled={loading}
      >
        {loading ? "Loading..." : "Load More"}
      </button>
    );
  }

  const visiblePages = getVisiblePages(currentPage, totalPages);

  return (
    <div className={styles.pagination}>
      <button
        type="button"
        className={styles.navButton}
        onClick={() => onPageChange(0)}
        disabled={currentPage === 0}
      >
        «
      </button>
      <button
        type="button"
        className={styles.navButton}
        onClick={() => onPageChange(currentPage - 1)}
        disabled={currentPage === 0}
      >
        ←
      </button>
      {visiblePages[0] > 0 && (
        <>
          <button
            type="button"
            className={styles.pageButton}
            onClick={() => onPageChange(0)}
          >
            1
          </button>
          {visiblePages[0] > 1 && <span className={styles.ellipsis}>…</span>}
        </>
      )}
      {visiblePages.map((i) => (
        <button
          type="button"
          key={i}
          className={`${styles.pageButton} ${i === currentPage ? styles.active : ""}`}
          onClick={() => onPageChange(i)}
        >
          {i + 1}
        </button>
      ))}
      {visiblePages[visiblePages.length - 1] < totalPages - 1 && (
        <>
          {visiblePages[visiblePages.length - 1] < totalPages - 2 && (
            <span className={styles.ellipsis}>…</span>
          )}
          <button
            type="button"
            className={styles.pageButton}
            onClick={() => onPageChange(totalPages - 1)}
          >
            {totalPages}
          </button>
        </>
      )}
      <button
        type="button"
        className={styles.navButton}
        onClick={() => onPageChange(currentPage + 1)}
        disabled={currentPage === totalPages - 1}
      >
        →
      </button>
      <button
        type="button"
        className={styles.navButton}
        onClick={() => onPageChange(totalPages - 1)}
        disabled={currentPage === totalPages - 1}
      >
        »
      </button>
    </div>
  );
}
