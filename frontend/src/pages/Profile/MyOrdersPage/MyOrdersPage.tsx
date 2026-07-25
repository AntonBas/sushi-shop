import { useState } from "react";
import { useApi } from "../../../hooks/common/useApi";
import * as ordersApi from "../../../api/orders";
import Loading from "../../../components/UI/Loading/Loading";
import Pagination from "../../../components/UI/Pagination/Pagination";
import type { UserOrderResponse } from "../../../types";
import type { Page } from "../../../types/common";
import {
  ORDER_STATUS_COLORS,
  ORDER_STATUS_LABELS,
  PAYMENT_STATUS_COLORS,
  PAYMENT_STATUS_LABELS,
} from "../../../types/enums";
import styles from "./MyOrdersPage.module.css";

export default function MyOrdersPage() {
  const { data, loading, execute } = useApi<Page<UserOrderResponse>>();
  const [orders, setOrders] = useState<UserOrderResponse[]>([]);
  const [page, setPage] = useState(0);
  const [expandedId, setExpandedId] = useState<number | null>(null);

  useState(() => {
    execute(() => ordersApi.getMyOrders(page)).then((res) =>
      setOrders(res.content),
    );
  });

  const loadPage = (p: number) => {
    setPage(p);
    execute(() => ordersApi.getMyOrders(p)).then((res) =>
      setOrders(res.content),
    );
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
                  onClick={() =>
                    setExpandedId(expandedId === order.id ? null : order.id)
                  }
                >
                  <div>
                    <span className={styles.orderId}>Order #{order.id}</span>
                    <span className={styles.orderDate}>
                      {new Date(order.createdAt).toLocaleDateString("uk-UA", {
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
                    <span className={styles.orderTotal}>
                      {order.totalAmount}₴
                    </span>
                    <span
                      className={styles.statusBadge}
                      style={{
                        background:
                          PAYMENT_STATUS_COLORS[order.paymentStatus] ||
                          "#64748b",
                      }}
                    >
                      {PAYMENT_STATUS_LABELS[order.paymentStatus]}
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

                {expandedId === order.id && (
                  <div className={styles.orderDetails}>
                    <div className={styles.itemsList}>
                      {order.items.map((item) => (
                        <div key={item.productId} className={styles.item}>
                          {item.mainImage && (
                            <img
                              src={item.mainImage}
                              alt={item.productName}
                              className={styles.itemImage}
                            />
                          )}
                          <span>
                            {item.productName} × {item.quantity}
                          </span>
                          <span>{item.price * item.quantity}₴</span>
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
            totalPages={data?.totalPages || 0}
            onPageChange={loadPage}
          />
        </>
      )}
    </div>
  );
}
