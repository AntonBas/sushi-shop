import HeroSection from "@/components/Home/HeroSection/HeroSection";
import PopularSection from "@/features/product/components/PopularSection/PopularSection";
import PromotionsSection from "@/features/promotion/components/PromotionsSection/PromotionsSection";
import styles from "./Home.module.css";

export default function Home() {
  return (
    <div className={styles.container}>
      <HeroSection />
      <PopularSection />
      <PromotionsSection />
    </div>
  );
}
