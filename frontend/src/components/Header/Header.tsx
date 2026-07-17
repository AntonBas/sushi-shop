import { useState, useEffect, useRef } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { useTheme } from '../../hooks/common/useTheme'
import { useCart } from '../../hooks/features/useCart'
import { ShoppingCart, Sun, Moon, User, LogOut, ChevronDown, Menu as MenuIcon } from 'lucide-react'
import styles from './Header.module.css'

export default function Header() {
  const { user, isAuthenticated, isAdmin, logout } = useAuth()
  const { theme, toggleTheme } = useTheme()
  const { count } = useCart()
  const location = useLocation()
  const navigate = useNavigate()
  const [isDropdownOpen, setIsDropdownOpen] = useState(false)
  const [isMobileOpen, setIsMobileOpen] = useState(false)
  const dropdownRef = useRef<HTMLDivElement>(null)
  const mobileRef = useRef<HTMLDivElement>(null)

  const isActive = (path: string) => location.pathname === path

  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target as Node)) setIsDropdownOpen(false)
      if (mobileRef.current && !mobileRef.current.contains(e.target as Node)) setIsMobileOpen(false)
    }
    document.addEventListener('mousedown', handleClickOutside)
    return () => document.removeEventListener('mousedown', handleClickOutside)
  }, [])

  const handleLogout = () => {
    logout()
    setIsDropdownOpen(false)
    setIsMobileOpen(false)
    navigate('/login')
  }

  return (
    <header className={styles.header}>
      <div className={styles.container}>
        <Link to="/" className={styles.logo}>Sushi Shop</Link>

        <nav className={styles.nav}>
          <Link to="/" className={`${styles.link} ${isActive('/') ? styles.active : ''}`}>
            <MenuIcon size={18} /> Menu
          </Link>
          <Link to="/cart" className={`${styles.link} ${isActive('/cart') ? styles.active : ''}`}>
            <ShoppingCart size={18} />
            Cart
            {count > 0 && <span className={styles.badge}>{count}</span>}
          </Link>

          <button onClick={toggleTheme} className={styles.iconBtn}>
            {theme === 'light' ? <Moon size={18} /> : <Sun size={18} />}
          </button>

          {isAuthenticated ? (
            <div className={styles.dropdown} ref={dropdownRef}>
              <button onClick={() => setIsDropdownOpen(!isDropdownOpen)} className={styles.iconBtn}>
                <User size={18} />
                <span className={styles.userName}>{user?.name?.split(' ')[0]}</span>
                <ChevronDown size={14} />
              </button>
              {isDropdownOpen && (
                <div className={styles.dropdownMenu}>
                  <div className={styles.dropdownUser}>
                    <strong>{user?.name}</strong>
                    <span>{user?.email}</span>
                  </div>
                  <hr />
                  <Link to="/profile" onClick={() => setIsDropdownOpen(false)}>
                    <User size={14} /> My Profile
                  </Link>
                  <Link to="/profile/orders" onClick={() => setIsDropdownOpen(false)}>
                    <ShoppingCart size={14} /> My Orders
                  </Link>
                  {isAdmin && (
                    <Link to="/admin" onClick={() => setIsDropdownOpen(false)}>Admin Panel</Link>
                  )}
                  <hr />
                  <button onClick={handleLogout}>
                    <LogOut size={14} /> Logout
                  </button>
                </div>
              )}
            </div>
          ) : (
            <Link to="/login" className={styles.loginBtn}>Login</Link>
          )}

          <button onClick={() => setIsMobileOpen(!isMobileOpen)} className={styles.mobileBtn}>
            <MenuIcon size={24} />
          </button>
        </nav>
      </div>

      {isMobileOpen && (
        <div className={styles.mobileMenu} ref={mobileRef}>
          <Link to="/" onClick={() => setIsMobileOpen(false)}>Menu</Link>
          <Link to="/cart" onClick={() => setIsMobileOpen(false)}>Cart ({count})</Link>
          {isAuthenticated ? (
            <>
              <Link to="/profile" onClick={() => setIsMobileOpen(false)}>My Profile</Link>
              <Link to="/profile/orders" onClick={() => setIsMobileOpen(false)}>My Orders</Link>
              {isAdmin && <Link to="/admin/products" onClick={() => setIsMobileOpen(false)}>Admin Panel</Link>}
              <button onClick={handleLogout}>Logout</button>
            </>
          ) : (
            <Link to="/login" onClick={() => setIsMobileOpen(false)}>Login</Link>
          )}
        </div>
      )}
    </header>
  )
}