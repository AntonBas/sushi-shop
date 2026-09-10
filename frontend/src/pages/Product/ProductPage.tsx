import { useState, useEffect } from "react";
import { useParams, Link } from "react-router-dom";
import {
  Star,
  ShoppingCart,
  Menu as MenuIcon,
  ChevronLeft,
  ChevronRight,
} from "lucide-react";
import Zoom from "react-medium-image-zoom";
import "react-medium-image-zoom/dist/styles.css";
import { useProducts } from "../../hooks/features/useProducts";
import { useCart } from "../../context/useCart";
import { useNotification } from "../../context/useNotification";
import { CATEGORY_DISPLAY } from "../../types/enums";
import ProductSkeleton from "../../components/Product/ProductSkeleton/ProductSkeleton";
import Button from "../../components/UI/Button/Button";
import ReviewSection from "../../components/Product/ReviewSection/ReviewSection";
import RelatedProducts from "../../components/Product/RelatedProducts/RelatedProducts";
import styles from "./ProductPage.module.css";

export default function ProductPage() {
  const { slug } = useParams<{ slug: string }>();
  const { product, productLoading, getProductBySlug } = useProducts();
  const { addItem } = useCart();
  const { showNotification } = useNotification();
  const [quantity, setQuantity] = useState(1);
  const [activeImage, setActiveImage] = useState(0);

  useEffect(() => {
    if (slug) getProductBySlug(slug);
    setQuantity(1);
    setActiveImage(0);
  }, [slug]);

  if (productLoading) return <ProductSkeleton />;
  if (!product) return null;

  const handleAddToCart = () => {
    addItem({
      productId: product.id,
      name: product.name,
      price: product.discountedPrice || product.price,
      quantity,
      mainImage: product.images[0]?.url || null,
    });
    showNotification(`${product.name} added to cart`, "success");
  };

  const discounted =
    product.discountedPrice && product.discountedPrice < product.price;

  const prevImage = () => {
    setActiveImage((prev) =>
      prev === 0 ? product.images.length - 1 : prev - 1,
    );
  };

  const nextImage = () => {
    setActiveImage((prev) =>
      prev === product.images.length - 1 ? 0 : prev + 1,
    );
  };

  return (
    <div className={styles.page}>
      <div className={styles.breadcrumbs}>
        <Link to="/menu" className={styles.breadcrumbLink}>
          <MenuIcon size={14} /> Menu
        </Link>
        <span className={styles.separator}>/</span>
        <Link
          to={`/menu?category=${product.category}`}
          className={styles.breadcrumbLink}
        >
          {CATEGORY_DISPLAY[product.category]}
        </Link>
        <span className={styles.separator}>/</span>
        <span className={styles.breadcrumb}>{product.name}</span>
      </div>

      <div className={styles.layout}>
        <div className={styles.images}>
          <div className={styles.mainImageWrapper}>
            <Zoom>
              <img
                src={product.images[activeImage]?.url || "/placeholder.jpg"}
                alt={product.name}
                className={styles.mainImage}
              />
            </Zoom>
            {product.images.length > 1 && (
              <>
                <button
                  onClick={prevImage}
                  className={`${styles.arrow} ${styles.arrowLeft}`}
                >
                  <ChevronLeft size={24} />
                </button>
                <button
                  onClick={nextImage}
                  className={`${styles.arrow} ${styles.arrowRight}`}
                >
                  <ChevronRight size={24} />
                </button>
              </>
            )}
          </div>
          {product.images.length > 1 && (
            <div className={styles.thumbnails}>
              {product.images.map((img, i) => (
                <button
                  key={img.id}
                  onClick={() => setActiveImage(i)}
                  className={`${styles.thumb} ${i === activeImage ? styles.activeThumb : ""}`}
                >
                  <img src={img.url} alt="" loading="lazy" />
                </button>
              ))}
            </div>
          )}
        </div>

        <div className={styles.info}>
          <span className={styles.category}>
            {CATEGORY_DISPLAY[product.category]}
          </span>
          <h1 className={styles.name}>{product.name}</h1>

          {product.averageRating && (
            <div className={styles.rating}>
              <Star size={16} fill="#fbbf24" stroke="#fbbf24" />
              <span>{product.averageRating.toFixed(1)}</span>
              <span className={styles.reviewCount}>
                ({product.reviewCount} reviews)
              </span>
            </div>
          )}

          <div className={styles.priceRow}>
            {discounted && (
              <>
                <span className={styles.oldPrice}>{product.price}₴</span>
                <span className={styles.discount}>
                  -{product.discountPercent}%
                </span>
              </>
            )}
            <span className={styles.price}>
              {product.discountedPrice || product.price}₴
            </span>
          </div>

          <div className={styles.details}>
            {product.weight && (
              <span className={styles.detail}>
                {product.weight}
                {product.category === "DRINK" ? "ml" : "g"}
              </span>
            )}
            {product.pieces && (
              <span className={styles.detail}>{product.pieces} pieces</span>
            )}
          </div>

          {product.promotionTitle && (
            <div className={styles.promo}>{product.promotionTitle}</div>
          )}

          {product.description && (
            <p className={styles.description}>{product.description}</p>
          )}

          <div className={styles.actions}>
            <div className={styles.quantity}>
              <button onClick={() => setQuantity((q) => Math.max(1, q - 1))}>
                −
              </button>
              <span>{quantity}</span>
              <button onClick={() => setQuantity((q) => q + 1)}>+</button>
            </div>
            <Button onClick={handleAddToCart} className={styles.addBtn}>
              <ShoppingCart size={18} /> Add to Cart —{" "}
              {(product.discountedPrice || product.price) * quantity}₴
            </Button>
          </div>
        </div>
      </div>

      <RelatedProducts productId={product.id} />
      <ReviewSection productId={product.id} />
    </div>
  );
}
