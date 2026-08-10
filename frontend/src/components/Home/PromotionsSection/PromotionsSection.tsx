import { Link } from "react-router-dom";
import { Tag, Clock } from "lucide-react";
import { usePromotions } from "../../../hooks/features/usePromotions";
import styles from "./PromotionsSection.module.css";

export default function PromotionsSection() {
  const { promotions } = usePromotions();

  if (promotions.length === 0) return null;

  return (
    <section className={styles.section}>
      <h2 className={styles.title}>Special Offers</h2>
      <div className={styles.list}>
        {promotions.map((promo) => {
          const daysLeft = Math.ceil(
            (new Date(promo.endDate).getTime() - Date.now()) /
              (1000 * 60 * 60 * 24),
          );

          return (
            <Link
              to={`/promotions/${promo.slug}`}
              key={promo.id}
              className={styles.card}
            >
              <div className={styles.cardContent}>
                <div className={styles.badge}>
                  <Tag size={16} />
                  <span>PROMOTION</span>
                </div>
                <h3 className={styles.cardTitle}>{promo.title}</h3>
                {promo.description && (
                  <p className={styles.cardDesc}>{promo.description}</p>
                )}
                <div className={styles.cardMeta}>
                  <span className={styles.productCount}>
                    {promo.products.length} product
                    {promo.products.length !== 1 ? "s" : ""}
                  </span>
                  <span className={styles.daysLeft}>
                    <Clock size={14} />
                    {daysLeft} day{daysLeft !== 1 ? "s" : ""} left
                  </span>
                </div>
              </div>
              <div className={styles.discountBadge}>
                <span className={styles.discountValue}>
                  {promo.discountPercent}%
                </span>
                <span className={styles.discountLabel}>OFF</span>
              </div>
            </Link>
          );
        })}
      </div>
    </section>
  );
}
