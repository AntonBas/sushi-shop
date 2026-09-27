import { useState, useEffect, useRef, useCallback } from "react";
import { useApi } from "../../../hooks/common/useApi";
import { useNotification } from "../../../context/useNotification";
import { getErrorMessage } from "../../../api/errorMessage";
import { useOrderSocket } from "../../../hooks/features/useOrderSocket";
import * as ordersApi from "../../../api/orders";
import * as paymentsApi from "../../../api/payments";
import { formatPrice } from "../../../utils/formatPrice";
import Loading from "../../../components/UI/Loading/Loading";
import Pagination from "../../../components/UI/Pagination/Pagination";
import type { OrderStatusUpdateResponse, UserOrderResponse } from "../../../types";
import type { Page } from "../../../types/common";
import {
  ORDER_STATUS_COLORS,
  ORDER_STATUS_LABELS,
  PAYMENT_STATUS_LABELS,
} from "../../../types/enums";
import type { Client, StompSubscription } from "@stomp/stompjs";
import styles from "./MyOrdersPage.module.css";

export default function MyOrdersPage() {
  const { showNotification } = useNotification();
  const { data, loading, run } = useApi<Page<UserOrderResponse>>();
  const [orders, setOrders] = useState<UserOrderResponse[]>([]);
  const [page, setPage] = useState(0);
  const [expandedId, setExpandedId] = useState<number | null>(null);
  const [payLoading, setPayLoading] = useState<number | null>(null);
  const ordersRef = useRef<UserOrderResponse[]>([]);
  const subscriptionsRef = useRef<Map<number, StompSubscription>>(new Map());

  const toggleExpanded = (id: number) =>
    setExpandedId((current) => (current === id ? null : id));

  useEffect(() => {
    void run(() => ordersApi.getMyOrders(page)).then((res) => {
      if (res) setOrders(res.content);
    });
  }, [page, run]);

  const syncSubscriptions = useCallback((client: Client) => {
    if (!client.connected) return;

    const currentIds = new Set(ordersRef.current.map((order) => order.id));

    subscriptionsRef.current.forEach((subscription, id) => {
      if (!currentIds.has(id)) {
        subscription.unsubscribe();
        subscriptionsRef.current.delete(id);
      }
    });

    currentIds.forEach((id) => {
      if (subscriptionsRef.current.has(id)) return;
      const subscription = client.subscribe(`/topic/orders/${id}`, (message) => {
        const update = JSON.parse(message.body) as OrderStatusUpdateResponse;
        setOrders((prev) =>
          prev.map((o) =>
            o.id === update.orderId ? { ...o, status: update.status } : o,
          ),
        );
      });
      subscriptionsRef.current.set(id, subscription);
    });
  }, []);

  const stompRef = useOrderSocket((client: Client) => {
    subscriptionsRef.current.clear();
    syncSubscriptions(client);
  });

  useEffect(() => {
    ordersRef.current = orders;
    const client = stompRef.current;
    if (client) syncSubscriptions(client);
  }, [orders, syncSubscriptions, stompRef]);

  const handlePay = async (order: UserOrderResponse) => {
    setPayLoading(order.id);
    try {
      const url = await paymentsApi.createCheckout(order.id);
      if (url) window.location.assign(url);
    } catch (err) {
      showNotification(getErrorMessage(err, "Could not start payment"), "error");
    } finally {
      setPayLoading(null);
    }
  };

  if (loading) return <Loading text="Loading orders..." />;

  return (
    <div className={styles.page}>
      <h1 className={styles.title}>My Orders</h1>

      {orders.length === 0 ? (
        <div className={styles.empty}>
          <h3>No orders yet</h3>
          <p>Your order history will appear here</p>
        </div>
      ) : (
        <>
          <div className={styles.orderList}>
            {orders.map((order) => (
              <div key={order.id} className={styles.orderCard}>
                <div
                  className={styles.orderHeader}
                  role="button"
                  tabIndex={0}
                  aria-expanded={expandedId === order.id}
                  aria-controls={`order-details-${order.id}`}
                  onClick={() => toggleExpanded(order.id)}
                  onKeyDown={(e) => {
                    if (e.key === "Enter" || e.key === " ") {
                      e.preventDefault();
                      toggleExpanded(order.id);
                    }
                  }}
                >
                  <div>
                    <span className={styles.orderId}>Order #{order.id}</span>
                    <span className={styles.orderDate}>
                      {new Date(order.createdAt).toLocaleString("en-US", {
                        day: "numeric",
                        month: "long",
                        year: "numeric",
                        hour: "2-digit",
                        minute: "2-digit",
                      })}
                    </span>
                  </div>
                  <div className={styles.orderHeaderRight}>
                    <span className={styles.methodBadge}>
                      {order.deliveryMethod === "DELIVERY"
                        ? "Delivery"
                        : "Pickup"}
                    </span>
                    <span className={styles.methodBadge}>
                      {PAYMENT_STATUS_LABELS[order.paymentStatus] ||
                        order.paymentStatus}
                    </span>
                    <span className={styles.orderTotal}>
                      {formatPrice(order.totalAmount)}₴
                    </span>
                    <span
                      className={styles.statusBadge}
                      style={{
                        background:
                          ORDER_STATUS_COLORS[order.status] || "#64748b",
                      }}
                    >
                      {ORDER_STATUS_LABELS[order.status]}
                    </span>
                  </div>
                </div>

                {order.status === "NEW" && order.paymentMethod === "ONLINE" && (
                  <div className={styles.paySection}>
                    <button
                      onClick={(e) => {
                        e.stopPropagation();
                        void handlePay(order);
                      }}
                      disabled={payLoading === order.id}
                      className={styles.payBtn}
                    >
                      {payLoading === order.id ? "Loading..." : "Pay Now"}
                    </button>
                  </div>
                )}

                {expandedId === order.id && (
                  <div id={`order-details-${order.id}`} className={styles.orderDetails}>
                    <div className={styles.itemsList}>
                      {order.items.map((item) => (
                        <div key={item.productId} className={styles.item}>
                          {item.mainImage && (
                            <img
                              src={item.mainImage}
                              alt={item.productName}
                              className={styles.itemImage}
                              loading="lazy"
                            />
                          )}
                          <span>
                            {item.productName} × {item.quantity}
                          </span>
                          <span>{formatPrice(item.unitPrice * item.quantity)}₴</span>
                        </div>
                      ))}
                    </div>
                  </div>
                )}
              </div>
            ))}
          </div>

          <Pagination
            currentPage={page}
            totalPages={data?.page.totalPages || 0}
            onPageChange={setPage}
          />
        </>
      )}
    </div>
  );
}
