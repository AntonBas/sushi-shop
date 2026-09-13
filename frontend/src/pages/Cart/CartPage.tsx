import { Link } from "react-router-dom";
import { Trash2, ShoppingBag } from "lucide-react";
import { useCart } from "../../context/useCart";
import Button from "../../components/UI/Button/Button";
import styles from "./CartPage.module.css";

export default function CartPage() {
  const { items, removeItem, clearCart, total, count } = useCart();

  if (items.length === 0) {
    return (
      <div className={styles.page}>
        <h1 className={styles.title}>Cart</h1>
        <div className={styles.empty}>
          <ShoppingBag size={48} className={styles.emptyIcon} />
          <h2>Your cart is empty</h2>
          <p>Add some sushi to get started</p>
          <Link to="/">
            <Button>Browse Menu</Button>
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className={styles.page}>
      <div className={styles.header}>
        <h1 className={styles.title}>Cart ({count})</h1>
        <button onClick={clearCart} className={styles.clearBtn}>
          Clear
        </button>
      </div>

      <div className={styles.items}>
        {items.map((item) => (
          <div key={item.productId} className={styles.item}>
            {item.mainImage && (
              <img
                src={item.mainImage}
                alt={item.name}
                className={styles.itemImage}
                loading="lazy"
              />
            )}
            <div className={styles.itemInfo}>
              <h3>{item.name}</h3>
              <span className={styles.itemPrice}>{item.price}₴</span>
            </div>
            <div className={styles.itemActions}>
              <span className={styles.quantity}>× {item.quantity}</span>
              <span className={styles.itemTotal}>
                {item.price * item.quantity}₴
              </span>
              <button
                type="button"
                onClick={() => removeItem(item.productId)}
                className={styles.removeBtn}
                aria-label={`Remove ${item.name} from cart`}
              >
                <Trash2 size={16} />
              </button>
            </div>
          </div>
        ))}
      </div>

      <div className={styles.footer}>
        <div className={styles.total}>
          <span>Total</span>
          <span className={styles.totalPrice}>{total}₴</span>
        </div>
        <Link to="/checkout">
          <Button style={{ width: "100%" }}>Proceed to Checkout</Button>
        </Link>
      </div>
    </div>
  );
}
