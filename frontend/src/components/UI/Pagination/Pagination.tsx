import styles from "./Pagination.module.css";

interface Props {
  currentPage: number;
  totalPages: number;
  onPageChange: (page: number) => void;
  variant?: "pages" | "load-more";
  loading?: boolean;
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

  return (
    <div className={styles.pagination}>
      <button
        type="button"
        className={styles.navButton}
        onClick={() => onPageChange(currentPage - 1)}
        disabled={currentPage === 0}
      >
        ←
      </button>
      {Array.from({ length: totalPages }, (_, i) => (
        <button
          type="button"
          key={i}
          className={`${styles.pageButton} ${i === currentPage ? styles.active : ""}`}
          onClick={() => onPageChange(i)}
        >
          {i + 1}
        </button>
      ))}
      <button
        type="button"
        className={styles.navButton}
        onClick={() => onPageChange(currentPage + 1)}
        disabled={currentPage === totalPages - 1}
      >
        →
      </button>
    </div>
  );
}
