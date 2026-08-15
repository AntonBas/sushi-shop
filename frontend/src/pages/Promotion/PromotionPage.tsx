import { useEffect } from "react";
import { useParams, Link } from "react-router-dom";
import { Tag, Clock, Menu as MenuIcon } from "lucide-react";
import { useApi } from "../../hooks/common/useApi";
import * as promotionsApi from "../../api/promotions";
import ProductCard from "../../components/Product/ProductCard/ProductCard";
import Loading from "../../components/UI/Loading/Loading";
import type { PromotionResponse } from "../../types";
import styles from "./PromotionPage.module.css";

export default function PromotionPage() {
  const { slug } = useParams<{ slug: string }>();
  const { data: promotion, loading, execute } = useApi<PromotionResponse>();

  useEffect(() => {
    if (slug) execute(() => promotionsApi.getPromotionBySlug(slug));
  }, [slug, execute]);

  if (loading) return <Loading text="Loading promotion..." />;
  if (!promotion) return null;

  const daysLeft = Math.ceil(
    (new Date(promotion.endDate).getTime() - Date.now()) /
      (1000 * 60 * 60 * 24),
  );

  return (
    <div className={styles.page}>
      <div className={styles.breadcrumbs}>
        <Link to="/" className={styles.breadcrumbLink}>
          <MenuIcon size={14} /> Menu
        </Link>
        <span className={styles.separator}>/</span>
        <span className={styles.breadcrumb}>{promotion.title}</span>
      </div>

      <div className={styles.header}>
        <div className={styles.headerInfo}>
          <div className={styles.badge}>
            <Tag size={16} />
            <span>PROMOTION</span>
          </div>
          <h1 className={styles.title}>{promotion.title}</h1>
          {promotion.description && (
            <p className={styles.description}>{promotion.description}</p>
          )}
          <div className={styles.meta}>
            <span className={styles.daysLeft}>
              <Clock size={14} />
              {daysLeft} day{daysLeft !== 1 ? "s" : ""} left
            </span>
            <span className={styles.productCount}>
              {promotion.products.length} product
              {promotion.products.length !== 1 ? "s" : ""}
            </span>
          </div>
        </div>
        <div className={styles.discountBadge}>
          <span className={styles.discountValue}>
            {promotion.discountPercent}%
          </span>
          <span className={styles.discountLabel}>OFF</span>
        </div>
      </div>

      <div className={styles.products}>
        {promotion.products.map((product) => (
          <ProductCard key={product.id} product={product} />
        ))}
      </div>
    </div>
  );
}
