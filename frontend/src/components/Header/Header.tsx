import { useState, useEffect, useRef } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../../context/useAuth";
import { useTheme } from "../../hooks/common/useTheme";
import { useCart } from "../../context/useCart";
import {
  ShoppingCart,
  Sun,
  Moon,
  User,
  LogOut,
  ChevronDown,
  Menu as MenuIcon,
  Shield,
} from "lucide-react";
import styles from "./Header.module.css";

export default function Header() {
  const { user, isAuthenticated, isAdmin, isCourier, logout } = useAuth();
  const { theme, toggleTheme } = useTheme();
  const { count } = useCart();
  const location = useLocation();
  const navigate = useNavigate();
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const [isMobileOpen, setIsMobileOpen] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);
  const mobileRef = useRef<HTMLDivElement>(null);

  const isActive = (path: string) => location.pathname === path;

  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (
        dropdownRef.current &&
        !dropdownRef.current.contains(e.target as Node)
      )
        setIsDropdownOpen(false);
      if (mobileRef.current && !mobileRef.current.contains(e.target as Node))
        setIsMobileOpen(false);
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key !== "Escape") return;
      setIsDropdownOpen(false);
      setIsMobileOpen(false);
    };
    document.addEventListener("keydown", handleKeyDown);
    return () => document.removeEventListener("keydown", handleKeyDown);
  }, []);

  const handleLogout = () => {
    logout();
    setIsDropdownOpen(false);
    setIsMobileOpen(false);
    navigate("/login");
  };

  const adminLink = isCourier ? "/admin/orders" : "/admin/products";

  return (
    <header className={styles.header}>
      <div className={styles.container}>
        <Link to="/" className={styles.logo}>
          Sushi Bas Shop
        </Link>

        <nav className={styles.nav}>
          <Link
            to="/menu"
            className={`${styles.link} ${isActive("/menu") ? styles.active : ""}`}
          >
            <MenuIcon size={18} /> Menu
          </Link>
          <Link
            to="/cart"
            className={`${styles.link} ${isActive("/cart") ? styles.active : ""}`}
          >
            <ShoppingCart size={18} />
            Cart
            {count > 0 && <span className={styles.badge}>{count}</span>}
          </Link>

          <button
            type="button"
            onClick={toggleTheme}
            className={styles.iconBtn}
            aria-label={theme === "light" ? "Switch to dark theme" : "Switch to light theme"}
          >
            {theme === "light" ? <Moon size={18} /> : <Sun size={18} />}
          </button>

          {isAuthenticated ? (
            <div className={styles.dropdown} ref={dropdownRef}>
              <button
                type="button"
                onClick={() => setIsDropdownOpen(!isDropdownOpen)}
                className={styles.iconBtn}
                aria-haspopup="menu"
                aria-expanded={isDropdownOpen}
              >
                <User size={18} />
                <span className={styles.userName}>
                  {user?.name?.split(" ")[0]}
                </span>
                <ChevronDown size={14} />
              </button>
              {isDropdownOpen && (
                <div className={styles.dropdownMenu} role="menu">
                  <div className={styles.dropdownUser}>
                    <strong>{user?.name}</strong>
                    <span>{user?.email}</span>
                  </div>
                  <hr />
                  <Link to="/profile" onClick={() => setIsDropdownOpen(false)} role="menuitem">
                    <User size={14} /> My Profile
                  </Link>
                  <Link
                    to="/profile/orders"
                    onClick={() => setIsDropdownOpen(false)}
                    role="menuitem"
                  >
                    <ShoppingCart size={14} /> My Orders
                  </Link>
                  {(isAdmin || isCourier) && (
                    <Link
                      to={adminLink}
                      onClick={() => setIsDropdownOpen(false)}
                      role="menuitem"
                    >
                      <Shield size={14} />{" "}
                      {isCourier ? "Orders" : "Admin Panel"}
                    </Link>
                  )}
                  <hr />
                  <button type="button" onClick={handleLogout} role="menuitem">
                    <LogOut size={14} /> Logout
                  </button>
                </div>
              )}
            </div>
          ) : (
            <Link to="/login" className={styles.loginBtn}>
              Login
            </Link>
          )}

          <button
            type="button"
            onClick={() => setIsMobileOpen(!isMobileOpen)}
            className={styles.mobileBtn}
            aria-label="Toggle menu"
            aria-expanded={isMobileOpen}
          >
            <MenuIcon size={24} />
          </button>
        </nav>
      </div>

      {isMobileOpen && (
        <div className={styles.mobileMenu} ref={mobileRef}>
          <Link to="/menu" onClick={() => setIsMobileOpen(false)}>
            Menu
          </Link>
          <Link to="/cart" onClick={() => setIsMobileOpen(false)}>
            Cart ({count})
          </Link>
          {isAuthenticated ? (
            <>
              <Link to="/profile" onClick={() => setIsMobileOpen(false)}>
                My Profile
              </Link>
              <Link to="/profile/orders" onClick={() => setIsMobileOpen(false)}>
                My Orders
              </Link>
              {(isAdmin || isCourier) && (
                <Link to={adminLink} onClick={() => setIsMobileOpen(false)}>
                  {isCourier ? "Orders" : "Admin Panel"}
                </Link>
              )}
              <button type="button" onClick={handleLogout}>Logout</button>
            </>
          ) : (
            <Link to="/login" onClick={() => setIsMobileOpen(false)}>
              Login
            </Link>
          )}
        </div>
      )}
    </header>
  );
}
