import { Outlet } from 'react-router-dom'
import ProfileSidebar from '@/features/user/components/ProfileSidebar/ProfileSidebar'
import styles from './ProfileLayout.module.css'

export default function ProfileLayout() {
  return (
    <div className={styles.layout}>
      <ProfileSidebar />
      <main className={styles.content}>
        <Outlet />
      </main>
    </div>
  )
}