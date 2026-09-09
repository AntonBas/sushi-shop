import { useState, useEffect, useRef } from "react";
import { useAdminOrders } from "../../../hooks/features/useAdminOrders";
import { useNotification } from "../../../context/NotificationContext";
import * as ordersApi from "../../../api/orders";
import Loading from "../../../components/UI/Loading/Loading";
import Pagination from "../../../components/UI/Pagination/Pagination";
import { Search } from "lucide-react";
import type {
  DeliveryMethod,
  OrderStatus,
  PaymentMethod,
} from "../../../types";
import {
  ORDER_STATUS_COLORS,
  ORDER_STATUS_LABELS,
  PAYMENT_STATUS_LABELS,
} from "../../../types/enums";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import styles from "./AdminOrdersPage.module.css";

const getStatusFlow = (
  currentStatus: OrderStatus,
  deliveryMethod: DeliveryMethod,
): OrderStatus[] => {
  switch (currentStatus) {
    case "NEW":
      return ["CONFIRMED", "CANCELLED"];
    case "CONFIRMED":
      return ["COOKING", "CANCELLED"];
    case "COOKING":
      return deliveryMethod === "DELIVERY" ? ["DELIVERING"] : ["READY"];
    case "DELIVERING":
      return ["DELIVERED"];
    case "READY":
      return ["DELIVERED"];
    default:
      return [];
  }
};

export default function AdminOrdersPage() {
  const { orders, totalPages, loading, loadOrders } = useAdminOrders();
  const { showNotification } = useNotification();
  const [page, setPage] = useState(0);
  const [statusFilter, setStatusFilter] = useState<OrderStatus | "">("");
  const [deliveryFilter, setDeliveryFilter] = useState<DeliveryMethod | "">("");
  const [paymentFilter, setPaymentFilter] = useState<PaymentMethod | "">("");
  const [search, setSearch] = useState("");
  const [expandedId, setExpandedId] = useState<number | null>(null);
  const stompRef = useRef<Client | null>(null);

  useEffect(() => {
    loadOrders(0);
  }, []);

  useEffect(() => {
    loadOrders(page, 12, {
      status: statusFilter || undefined,
      deliveryMethod: deliveryFilter || undefined,
      paymentMethod: paymentFilter || undefined,
      search: search || undefined,
    });
  }, [page, statusFilter, deliveryFilter, paymentFilter, search]);

  useEffect(() => {
    const client = new Client({
      webSocketFactory: () => new SockJS("/ws"),
      connectHeaders: {
        Authorization: `Bearer ${localStorage.getItem("token")}`,
      },
      onConnect: () => {
        client.subscribe("/topic/orders/new", () => {
          loadOrders(page, 12, {
            status: statusFilter || undefined,
            deliveryMethod: deliveryFilter || undefined,
            paymentMethod: paymentFilter || undefined,
            search: search || undefined,
          });
        });
      },
    });
    client.activate();
    stompRef.current = client;

    return () => {
      client.deactivate();
    };
  }, [page, statusFilter, deliveryFilter, paymentFilter, search]);

  const handleStatusChange = async (
    orderId: number,
    newStatus: OrderStatus,
  ) => {
    try {
      await ordersApi.updateOrderStatus(orderId, newStatus);
      showNotification(
        `Order #${orderId} → ${ORDER_STATUS_LABELS[newStatus]}`,
        "success",
      );
      loadOrders(page, 12, {
        status: statusFilter || undefined,
        deliveryMethod: deliveryFilter || undefined,
        paymentMethod: paymentFilter || undefined,
        search: search || undefined,
      });
    } catch (err: unknown) {
      const message =
        (err as { response?: { data?: { message?: string } } })?.response?.data
          ?.message || "Failed to update status";
      showNotification(message, "error");
    }
  };

  if (loading) return <Loading text="Loading orders..." />;

  return (
    <div className={styles.page}>
      <div className={styles.header}>
        <h1>Orders</h1>
      </div>

      <div className={styles.filters}>
        <div className={styles.searchBox}>
          <Search size={16} />
          <input
            type="text"
            placeholder="Search customer or phone..."
            value={search}
            onChange={(e) => {
              setSearch(e.target.value);
              setPage(0);
            }}
          />
        </div>
        <select
          value={statusFilter}
          onChange={(e) => {
            setStatusFilter(e.target.value as OrderStatus | "");
            setPage(0);
          }}
          className={styles.filterSelect}
        >
          <option value="">All Statuses</option>
          {[
            "NEW",
            "CONFIRMED",
            "COOKING",
            "DELIVERING",
            "READY",
            "DELIVERED",
            "CANCELLED",
          ].map((s) => (
            <option key={s} value={s}>
              {ORDER_STATUS_LABELS[s as OrderStatus]}
            </option>
          ))}
        </select>
        <select
          value={deliveryFilter}
          onChange={(e) => {
            setDeliveryFilter(e.target.value as DeliveryMethod | "");
            setPage(0);
          }}
          className={styles.filterSelect}
        >
          <option value="">All Methods</option>
          <option value="DELIVERY">Delivery</option>
          <option value="PICKUP">Pickup</option>
        </select>
        <select
          value={paymentFilter}
          onChange={(e) => {
            setPaymentFilter(e.target.value as PaymentMethod | "");
            setPage(0);
          }}
          className={styles.filterSelect}
        >
          <option value="">All Payments</option>
          <option value="ONLINE">Online</option>
          <option value="ON_DELIVERY">On Delivery</option>
        </select>
      </div>

      {orders.length === 0 ? (
        <div className={styles.empty}>
          <h3>No orders found</h3>
        </div>
      ) : (
        <>
          <div className={styles.tableWrapper}>
            <table className={styles.table}>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Customer</th>
                  <th>Method</th>
                  <th>Payment</th>
                  <th>Total</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {orders.map((order) => (
                  <>
                    <tr
                      key={order.id}
                      className={styles.orderRow}
                      onClick={() =>
                        setExpandedId(expandedId === order.id ? null : order.id)
                      }
                    >
                      <td data-label="ID">#{order.id}</td>
                      <td data-label="Customer">{order.customerName}</td>
                      <td data-label="Method">
                        {order.deliveryMethod === "DELIVERY"
                          ? "Delivery"
                          : "Pickup"}
                      </td>
                      <td data-label="Payment">
                        {PAYMENT_STATUS_LABELS[order.paymentStatus] ||
                          order.paymentStatus}
                      </td>
                      <td data-label="Total">{order.totalAmount}₴</td>
                      <td data-label="Status">
                        <span
                          className={styles.statusBadge}
                          style={{
                            background: ORDER_STATUS_COLORS[order.status],
                          }}
                        >
                          {ORDER_STATUS_LABELS[order.status]}
                        </span>
                      </td>
                      <td data-label="Actions">
                        <div className={styles.actions}>
                          {getStatusFlow(
                            order.status,
                            order.deliveryMethod,
                          ).map((nextStatus) => (
                            <button
                              key={nextStatus}
                              onClick={(e) => {
                                e.stopPropagation();
                                handleStatusChange(order.id, nextStatus);
                              }}
                              className={styles.actionBtn}
                              style={{
                                background: ORDER_STATUS_COLORS[nextStatus],
                              }}
                            >
                              {ORDER_STATUS_LABELS[nextStatus]}
                            </button>
                          ))}
                        </div>
                      </td>
                    </tr>
                    {expandedId === order.id && (
                      <tr className={styles.expandedRow}>
                        <td colSpan={7}>
                          <div className={styles.expandedContent}>
                            <div className={styles.detailRow}>
                              <span>Email:</span>
                              <span>{order.userEmail}</span>
                            </div>
                            <div className={styles.detailRow}>
                              <span>Phone:</span>
                              <span>{order.phone}</span>
                            </div>
                            {order.address?.city && (
                              <div className={styles.detailRow}>
                                <span>Address:</span>
                                <span>
                                  {order.address.city}, {order.address.street}{" "}
                                  {order.address.house}
                                  {order.address.apartment
                                    ? `, apt. ${order.address.apartment}`
                                    : ""}
                                </span>
                              </div>
                            )}
                            <div className={styles.itemsList}>
                              {order.items.map((item) => (
                                <div
                                  key={item.productId}
                                  className={styles.itemRow}
                                >
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
                        </td>
                      </tr>
                    )}
                  </>
                ))}
              </tbody>
            </table>
          </div>

          <Pagination
            currentPage={page}
            totalPages={totalPages}
            onPageChange={setPage}
          />
        </>
      )}
    </div>
  );
}
