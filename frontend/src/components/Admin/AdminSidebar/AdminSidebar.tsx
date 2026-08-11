import { useEffect } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import {
  UtensilsCrossed,
  Package,
  Tag,
  ScrollText,
  ArrowLeft,
} from "lucide-react";
import clsx from "clsx";
import { useAuth } from "../../../context/AuthContext";
import styles from "./AdminSidebar.module.css";

interface AdminSidebarProps {
  isOpen: boolean;
  isMobile: boolean;
  onClose: () => void;
}

export default function AdminSidebar({
  isOpen,
  isMobile,
  onClose,
}: AdminSidebarProps) {
  const location = useLocation();
  const navigate = useNavigate();
  const { isAdmin } = useAuth();

  const allMenuItems = [
    {
      path: "/admin/products",
      label: "Products",
      icon: UtensilsCrossed,
      adminOnly: true,
    },
    { path: "/admin/orders", label: "Orders", icon: Package, adminOnly: false },
    {
      path: "/admin/promotions",
      label: "Promotions",
      icon: Tag,
      adminOnly: true,
    },
    {
      path: "/admin/audit",
      label: "Audit Logs",
      icon: ScrollText,
      adminOnly: true,
    },
  ];

  const menuItems = allMenuItems.filter((item) => !item.adminOnly || isAdmin);

  useEffect(() => {
    if (isOpen && isMobile) {
      document.body.style.overflow = "hidden";
    } else {
      document.body.style.overflow = "auto";
    }
    return () => {
      document.body.style.overflow = "auto";
    };
  }, [isOpen, isMobile]);

  const handleItemClick = () => {
    if (isMobile) onClose();
  };

  return (
    <>
      {isOpen && isMobile && (
        <div className={styles.overlay} onClick={onClose} />
      )}

      <aside
        className={clsx(
          styles.sidebar,
          isOpen && styles.open,
          !isOpen && !isMobile && styles.closedDesktop,
          isMobile && styles.mobile,
        )}
      >
        <div className={styles.sidebarHeader}>
          <div className={styles.logoSection}>
            <div className={styles.logoIcon}>
              <UtensilsCrossed size={24} />
            </div>
            <div className={styles.logoText}>
              <h2 className={styles.logoTitle}>Sushi Shop</h2>
              <p className={styles.logoSubtitle}>Admin Panel</p>
            </div>
          </div>
        </div>

        <nav className={styles.nav}>
          {menuItems.map((item) => {
            const Icon = item.icon;
            return (
              <Link
                key={item.path}
                to={item.path}
                className={clsx(
                  styles.item,
                  location.pathname.startsWith(item.path) && styles.active,
                )}
                onClick={handleItemClick}
              >
                <Icon size={20} className={styles.icon} />
                <span className={styles.label}>{item.label}</span>
              </Link>
            );
          })}
        </nav>

        <div className={styles.sidebarFooter}>
          <button className={styles.backButton} onClick={() => navigate("/")}>
            <ArrowLeft size={18} />
            <span>Back to Website</span>
          </button>
        </div>
      </aside>
    </>
  );
}
