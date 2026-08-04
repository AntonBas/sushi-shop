import styles from "./ProductSkeleton.module.css";

export default function ProductSkeleton() {
  return (
    <div className={styles.page}>
      <div className={styles.breadcrumbs}>
        <div className={`${styles.skeleton} ${styles.breadcrumbSkel}`} />
      </div>
      <div className={styles.layout}>
        <div className={styles.images}>
          <div className={`${styles.skeleton} ${styles.mainImageSkel}`} />
          <div className={styles.thumbnailsSkel}>
            {[1, 2, 3, 4].map((i) => (
              <div
                key={i}
                className={`${styles.skeleton} ${styles.thumbSkel}`}
              />
            ))}
          </div>
        </div>
        <div className={styles.info}>
          <div className={`${styles.skeleton} ${styles.categorySkel}`} />
          <div className={`${styles.skeleton} ${styles.nameSkel}`} />
          <div className={`${styles.skeleton} ${styles.ratingSkel}`} />
          <div className={`${styles.skeleton} ${styles.priceSkel}`} />
          <div className={`${styles.skeleton} ${styles.detailsSkel}`} />
          <div className={`${styles.skeleton} ${styles.descSkel}`} />
          <div className={`${styles.skeleton} ${styles.descSkel}`} />
          <div className={`${styles.skeleton} ${styles.actionsSkel}`} />
        </div>
      </div>
    </div>
  );
}
