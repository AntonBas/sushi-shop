import { Link, useLocation } from 'react-router-dom'
import { User, Package, ArrowLeft } from 'lucide-react'
import clsx from 'clsx'
import styles from './ProfileSidebar.module.css'

export default function ProfileSidebar() {
  const location = useLocation()

  const menuItems = [
    { path: '/profile', label: 'My Profile', icon: User },
    { path: '/profile/orders', label: 'My Orders', icon: Package },
  ]

  return (
    <aside className={styles.sidebar}>
      <div className={styles.sidebarHeader}>
        <h2 className={styles.title}>Account</h2>
      </div>

      <nav className={styles.nav}>
        {menuItems.map((item) => {
          const Icon = item.icon
          return (
            <Link
              key={item.path}
              to={item.path}
              className={clsx(styles.item, location.pathname === item.path && styles.active)}
            >
              <Icon size={20} className={styles.icon} />
              <span>{item.label}</span>
            </Link>
          )
        })}
      </nav>

      <div className={styles.sidebarFooter}>
        <Link to="/" className={styles.backButton}>
          <ArrowLeft size={18} />
          <span>Back to Home</span>
        </Link>
      </div>
    </aside>
  )
}