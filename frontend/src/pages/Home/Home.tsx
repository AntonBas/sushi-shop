import HeroSection from "../../components/Home/HeroSection/HeroSection";
import PopularSection from "../../components/Home/PopularSection/PopularSection";
import PromotionsSection from "../../components/Home/PromotionsSection/PromotionsSection";
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
