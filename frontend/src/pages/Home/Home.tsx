import PromotionsSection from '../../components/Home/PromotionsSection/PromotionsSection'
import PopularSection from '../../components/Home/PopularSection/PopularSection'
import MenuSection from '../../components/Menu/MenuSection'
import styles from './Home.module.css'

export default function Home() {
  return (
    <div className={styles.container}>
      <MenuSection />
      <PopularSection />
      <PromotionsSection />
    </div>
  )
}