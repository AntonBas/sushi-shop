import { useState, useEffect, Suspense } from 'react'
import { Outlet } from 'react-router-dom'
import AdminSidebar from '../AdminSidebar/AdminSidebar'
import AdminHeader from '../AdminHeader/AdminHeader'
import Loading from '../../UI/Loading/Loading'
import styles from './AdminLayout.module.css'

const MOBILE_QUERY = '(max-width: 768px)'

export default function AdminLayout() {
  const [isMobile, setIsMobile] = useState(() => window.matchMedia(MOBILE_QUERY).matches)
  const [isSidebarOpen, setIsSidebarOpen] = useState(() => !window.matchMedia(MOBILE_QUERY).matches)

  useEffect(() => {
    const mediaQuery = window.matchMedia(MOBILE_QUERY)
    const handleBreakpointChange = (event: MediaQueryListEvent) => {
      setIsMobile(event.matches)
      setIsSidebarOpen(!event.matches)
    }
    mediaQuery.addEventListener('change', handleBreakpointChange)
    return () => mediaQuery.removeEventListener('change', handleBreakpointChange)
  }, [])

  const toggleSidebar = () => setIsSidebarOpen(!isSidebarOpen)
  const handleCloseSidebar = () => { if (isMobile) setIsSidebarOpen(false) }

  return (
    <div className={styles.adminLayout}>
      <AdminSidebar isOpen={isSidebarOpen} isMobile={isMobile} onClose={handleCloseSidebar} />

      <div className={`${styles.mainContent} ${!isSidebarOpen && styles.fullWidth}`}>
        <AdminHeader onToggleSidebar={toggleSidebar} isSidebarOpen={isSidebarOpen} />

        <main className={styles.content}>
          <Suspense fallback={<Loading text="Loading..." />}>
            <Outlet />
          </Suspense>
        </main>

        <footer className={styles.adminFooter}>
          <div className={styles.footerContent}>
            <div>© {new Date().getFullYear()} Sushi Shop Admin Panel</div>
          </div>
        </footer>
      </div>
    </div>
  )
}