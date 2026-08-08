import { useState, useEffect, useRef } from "react";
import { useApi } from "../../../hooks/common/useApi";
import * as ordersApi from "../../../api/orders";
import * as paymentsApi from "../../../api/payments";
import Loading from "../../../components/UI/Loading/Loading";
import Pagination from "../../../components/UI/Pagination/Pagination";
import type { UserOrderResponse } from "../../../types";
import type { Page } from "../../../types/common";
import {
  ORDER_STATUS_COLORS,
  ORDER_STATUS_LABELS,
  PAYMENT_STATUS_LABELS,
} from "../../../types/enums";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import styles from "./MyOrdersPage.module.css";

export default function MyOrdersPage() {
  const { data, loading, execute } = useApi<Page<UserOrderResponse>>();
  const [orders, setOrders] = useState<UserOrderResponse[]>([]);
  const [page, setPage] = useState(0);
  const [expandedId, setExpandedId] = useState<number | null>(null);
  const [payLoading, setPayLoading] = useState<number | null>(null);
  const stompRef = useRef<Client | null>(null);

  useEffect(() => {
    execute(() => ordersApi.getMyOrders(page)).then((res) =>
      setOrders(res.content),
    );
  }, [page]);

  useEffect(() => {
    const client = new Client({
      webSocketFactory: () => new SockJS("http://localhost:8080/ws"),
      onConnect: () => {
        orders.forEach((order) => {
          client.subscribe(`/topic/orders/${order.id}`, (message) => {
            const update = JSON.parse(message.body);
            setOrders((prev) =>
              prev.map((o) =>
                o.id === update.orderId ? { ...o, status: update.status } : o,
              ),
            );
          });
        });
      },
    });
    client.activate();
    stompRef.current = client;

    return () => {
      client.deactivate();
    };
  }, [orders.length]);

  const handlePay = async (order: UserOrderResponse) => {
    setPayLoading(order.id);
    try {
      const url = await paymentsApi.createCheckout(
        order.id,
        Math.round(order.totalAmount * 100),
        order.userEmail,
      );
      if (url) window.location.href = url;
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
                    <span className={styles.methodBadge}>
                      {PAYMENT_STATUS_LABELS[order.paymentStatus] ||
                        order.paymentStatus}
                    </span>
                    <span className={styles.orderTotal}>
                      {order.totalAmount}₴
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
                        handlePay(order);
                      }}
                      disabled={payLoading === order.id}
                      className={styles.payBtn}
                    >
                      {payLoading === order.id ? "Loading..." : "Pay Now"}
                    </button>
                  </div>
                )}

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
                          <span>{item.unitPrice * item.quantity}₴</span>
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
            onPageChange={setPage}
          />
        </>
      )}
    </div>
  );
}
