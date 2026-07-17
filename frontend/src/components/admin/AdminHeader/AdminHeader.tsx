import { useAuth } from '../../../context/AuthContext'
import styles from './AdminHeader.module.css'

interface AdminHeaderProps {
  onToggleSidebar: () => void
  isSidebarOpen: boolean
}

export default function AdminHeader({ onToggleSidebar, isSidebarOpen }: AdminHeaderProps) {
  const { user } = useAuth()

  return (
    <header className={styles.header}>
      <div className={styles.leftSection}>
        <button
          className={styles.menuButton}
          onClick={onToggleSidebar}
          aria-label="Toggle sidebar"
        >
          <div className={styles.hamburgerIcon}>
            <span className={isSidebarOpen ? styles.line1Open : styles.line1Closed}></span>
            <span className={isSidebarOpen ? styles.line2Open : styles.line2Closed}></span>
            <span className={isSidebarOpen ? styles.line3Open : styles.line3Closed}></span>
          </div>
        </button>
        <span className={styles.breadcrumbText}>Admin Panel</span>
      </div>

      <div className={styles.rightSection}>
        <div className={styles.userInfo}>
          <div className={styles.userDetails}>
            <span className={styles.userName}>{user?.name || 'Admin'}</span>
            <span className={styles.userRole}>{user?.userRole || 'Administrator'}</span>
          </div>
        </div>
      </div>
    </header>
  )
}