import { useState, useEffect } from "react";
import { useNavigate, Link } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";
import { useCart } from "../../hooks/features/useCart";
import { useOrders } from "../../hooks/features/useOrders";
import { useNotification } from "../../context/NotificationContext";
import * as paymentsApi from "../../api/payments";
import Button from "../../components/UI/Button/Button";
import Input from "../../components/UI/Input/Input";
import type { DeliveryMethod } from "../../types";
import styles from "./CheckoutPage.module.css";

export default function CheckoutPage() {
  const { user } = useAuth();
  const { items, total, clearCart } = useCart();
  const { createOrder, loading } = useOrders();
  const { showNotification } = useNotification();
  const navigate = useNavigate();

  const [customerName, setCustomerName] = useState("");
  const [phone, setPhone] = useState("");
  const [deliveryMethod, setDeliveryMethod] =
    useState<DeliveryMethod>("PICKUP");
  const [paymentMethod, setPaymentMethod] = useState<"cash" | "online">("cash");
  const [city, setCity] = useState("");
  const [street, setStreet] = useState("");
  const [house, setHouse] = useState("");
  const [apartment, setApartment] = useState("");

  useEffect(() => {
    if (user) {
      setCustomerName(user.name);
      setPhone(user.phone);
      if (user.address) {
        setCity(user.address.city);
        setStreet(user.address.street);
        setHouse(user.address.house);
        setApartment(user.address.apartment || "");
        setDeliveryMethod("DELIVERY");
      }
    }
  }, [user]);

  const handleSubmit = async (e: React.SyntheticEvent) => {
    e.preventDefault();
    try {
      const order = await createOrder({
        customerName,
        phone,
        deliveryMethod,
        address:
          deliveryMethod === "DELIVERY"
            ? { city, street, house, apartment: apartment || undefined }
            : undefined,
        items: items.map((i) => ({
          productId: i.productId,
          quantity: i.quantity,
        })),
      });
      if (order) {
        clearCart();
        if (paymentMethod === "online") {
          const url = await paymentsApi.createCheckout(
            order.id,
            Math.round(order.totalAmount * 100),
            user?.email || "",
          );
          window.location.href = url;
        } else {
          showNotification("Order placed successfully!", "success");
          navigate("/profile/orders");
        }
      }
    } catch {}
  };

  if (items.length === 0) {
    return (
      <div className={styles.page}>
        <h1 className={styles.title}>Checkout</h1>
        <div className={styles.empty}>
          <p>Your cart is empty</p>
          <Link to="/">
            <Button>Browse Menu</Button>
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className={styles.page}>
      <h1 className={styles.title}>Checkout</h1>

      <form onSubmit={handleSubmit} className={styles.form}>
        <div className={styles.section}>
          <h2 className={styles.sectionTitle}>Contact Info</h2>
          <Input
            label="Name"
            value={customerName}
            onChange={setCustomerName}
            placeholder="Your name"
          />
          <Input
            label="Phone"
            value={phone}
            onChange={setPhone}
            placeholder="+380991234567"
          />
        </div>

        <div className={styles.section}>
          <h2 className={styles.sectionTitle}>Delivery Method</h2>
          <div className={styles.methodButtons}>
            <button
              type="button"
              className={`${styles.methodBtn} ${deliveryMethod === "PICKUP" ? styles.activeMethod : ""}`}
              onClick={() => setDeliveryMethod("PICKUP")}
            >
              Pickup
            </button>
            <button
              type="button"
              className={`${styles.methodBtn} ${deliveryMethod === "DELIVERY" ? styles.activeMethod : ""}`}
              onClick={() => setDeliveryMethod("DELIVERY")}
            >
              Delivery
            </button>
          </div>
        </div>

        {deliveryMethod === "DELIVERY" && (
          <div className={styles.section}>
            <h2 className={styles.sectionTitle}>Delivery Address</h2>
            <div className={styles.addressGrid}>
              <Input
                label="City"
                value={city}
                onChange={setCity}
                placeholder="City"
              />
              <Input
                label="Street"
                value={street}
                onChange={setStreet}
                placeholder="Street"
              />
              <Input
                label="House"
                value={house}
                onChange={setHouse}
                placeholder="House"
              />
              <Input
                label="Apartment"
                value={apartment}
                onChange={setApartment}
                placeholder="Apt"
              />
            </div>
          </div>
        )}

        <div className={styles.section}>
          <h2 className={styles.sectionTitle}>Payment Method</h2>
          <div className={styles.methodButtons}>
            <button
              type="button"
              className={`${styles.methodBtn} ${paymentMethod === "cash" ? styles.activeMethod : ""}`}
              onClick={() => setPaymentMethod("cash")}
            >
              Pay on {deliveryMethod === "PICKUP" ? "Pickup" : "Delivery"}
            </button>
            <button
              type="button"
              className={`${styles.methodBtn} ${paymentMethod === "online" ? styles.activeMethod : ""}`}
              onClick={() => setPaymentMethod("online")}
            >
              Pay Online
            </button>
          </div>
        </div>

        <div className={styles.section}>
          <h2 className={styles.sectionTitle}>Order Summary</h2>
          <div className={styles.items}>
            {items.map((item) => (
              <div key={item.productId} className={styles.item}>
                <span>
                  {item.name} × {item.quantity}
                </span>
                <span>{item.price * item.quantity} ₴</span>
              </div>
            ))}
          </div>
          <div className={styles.total}>
            <span>Total</span>
            <span className={styles.totalPrice}>{total} ₴</span>
          </div>
        </div>

        <Button type="submit" loading={loading} style={{ width: "100%" }}>
          {paymentMethod === "online" ? "Proceed to Payment" : "Place Order"}
        </Button>
      </form>
    </div>
  );
}
