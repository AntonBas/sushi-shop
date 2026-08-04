import { useSearchParams, Link } from "react-router-dom";
import { XCircle } from "lucide-react";
import Button from "../../../components/UI/Button/Button";
import styles from "./CancelPage.module.css";

export default function OrderCancelPage() {
  const [searchParams] = useSearchParams();
  const orderId = searchParams.get("orderId");

  return (
    <div className={styles.page}>
      <div className={styles.card}>
        <XCircle size={64} className={styles.icon} />
        <h1>Payment Cancelled</h1>
        <p>Your payment was not completed.</p>
        {orderId && <p className={styles.orderId}>Order #{orderId}</p>}
        <div className={styles.actions}>
          <Link to="/cart">
            <Button>Back to Cart</Button>
          </Link>
          <Link to="/menu">
            <Button variant="secondary">Continue Shopping</Button>
          </Link>
        </div>
      </div>
    </div>
  );
}
