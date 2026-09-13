import { Link } from "react-router-dom";
import { Compass } from "lucide-react";
import Button from "../../components/UI/Button/Button";
import styles from "./NotFoundPage.module.css";

export default function NotFoundPage() {
  return (
    <div className={styles.page}>
      <div className={styles.card}>
        <Compass size={64} className={styles.icon} />
        <p className={styles.code}>404</p>
        <h1>Page Not Found</h1>
        <p>The page you're looking for doesn't exist or has been moved.</p>
        <div className={styles.actions}>
          <Link to="/">
            <Button>Back to Home</Button>
          </Link>
          <Link to="/menu">
            <Button variant="secondary">Browse Menu</Button>
          </Link>
        </div>
      </div>
    </div>
  );
}
