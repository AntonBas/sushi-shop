import PromotionsSection from '../../components/Home/PromotionsSection/PromotionsSection'
import PopularSection from '../../components/Home/PopularSection/PopularSection'
import MenuSection from '../../components/Home/MenuSection/MenuSection'
import styles from './Home.module.css'

export default function Home() {
  return (
    <div className={styles.container}>
      <PromotionsSection />
      <PopularSection />
      <MenuSection />
    </div>
  )
}