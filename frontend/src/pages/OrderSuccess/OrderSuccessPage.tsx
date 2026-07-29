import { useEffect } from "react";
import { useSearchParams, Link } from "react-router-dom";
import { CheckCircle2 } from "lucide-react";
import { useCart } from "../../context/CartContext";
import Button from "../../components/UI/Button/Button";
import styles from "./OrderSuccessPage.module.css";

export default function OrderSuccessPage() {
  const [searchParams] = useSearchParams();
  const orderId = searchParams.get("orderId");
  const { clearCart } = useCart();

  useEffect(() => {
    clearCart();
  }, []);

  return (
    <div className={styles.page}>
      <div className={styles.card}>
        <CheckCircle2 size={64} className={styles.icon} />
        <h1>Payment Successful!</h1>
        <p>Thank you for your order.</p>
        {orderId && <p className={styles.orderId}>Order #{orderId}</p>}
        <div className={styles.actions}>
          <Link to={`/profile/orders`}>
            <Button>View My Orders</Button>
          </Link>
          <Link to="/menu">
            <Button variant="secondary">Continue Shopping</Button>
          </Link>
        </div>
      </div>
    </div>
  );
}
