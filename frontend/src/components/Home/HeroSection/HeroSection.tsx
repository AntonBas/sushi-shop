import { Link } from "react-router-dom";
import { ArrowRight, Star } from "lucide-react";
import styles from "./HeroSection.module.css";

export default function HeroSection() {
  return (
    <section className={styles.hero}>
      <div className={styles.bgGlow} />
      <div className={styles.content}>
        <div className={styles.badge}>
          <Star size={14} fill="#fbbf24" stroke="#fbbf24" />
          <span>Delicious Sushi in Your City</span>
        </div>
        <h1 className={styles.title}>
          Premium Sushi <br />
          <span className={styles.highlight}>Delivered Fresh</span>
        </h1>
        <p className={styles.subtitle}>
          Handcrafted by expert chefs, delivered to your door in 30 minutes
        </p>
        <div className={styles.actions}>
          <Link to="/menu" className={styles.btnPrimary}>
            Order Now <ArrowRight size={18} />
          </Link>
          <Link to="/menu?category=SET" className={styles.btnSecondary}>
            View Sets
          </Link>
        </div>
      </div>
    </section>
  );
}
