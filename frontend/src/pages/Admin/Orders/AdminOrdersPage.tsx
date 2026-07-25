import { useState, useEffect } from "react";
import { useAdminOrders } from "../../../hooks/features/useAdmin";
import { useNotification } from "../../../context/NotificationContext";
import * as ordersApi from "../../../api/orders";
import Loading from "../../../components/UI/Loading/Loading";
import Pagination from "../../../components/UI/Pagination/Pagination";
import type { OrderStatus } from "../../../types";
import {
  ORDER_STATUS_COLORS,
  ORDER_STATUS_LABELS,
  PAYMENT_STATUS_COLORS,
  PAYMENT_STATUS_LABELS,
} from "../../../types/enums";
import styles from "./AdminOrdersPage.module.css";

const STATUS_FLOW: Record<OrderStatus, OrderStatus[]> = {
  NEW: ["CONFIRMED", "CANCELLED"],
  CONFIRMED: ["COOKING", "CANCELLED"],
  COOKING: ["DELIVERING", "READY"],
  DELIVERING: ["DELIVERED"],
  READY: ["DELIVERED"],
  DELIVERED: [],
  CANCELLED: [],
};

export default function AdminOrdersPage() {
  const { orders, totalPages, loading, loadOrders } = useAdminOrders();
  const { showNotification } = useNotification();
  const [page, setPage] = useState(0);
  const [statusFilter, setStatusFilter] = useState<OrderStatus | "">("");
  const [expandedId, setExpandedId] = useState<number | null>(null);

  useEffect(() => {
    loadOrders(0);
  }, []);

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
      loadOrders(page);
    } catch {
      showNotification("Failed to update status", "error");
    }
  };

  const filteredOrders = statusFilter
    ? orders.filter((o) => o.status === statusFilter)
    : orders;

  if (loading) return <Loading text="Loading orders..." />;

  return (
    <div className={styles.page}>
      <div className={styles.header}>
        <h1>Orders</h1>
      </div>

      <div className={styles.filters}>
        <select
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value as OrderStatus | "")}
          className={styles.filterSelect}
        >
          <option value="">All Statuses</option>
          {Object.keys(STATUS_FLOW).map((s) => (
            <option key={s} value={s}>
              {ORDER_STATUS_LABELS[s as OrderStatus]}
            </option>
          ))}
        </select>
      </div>

      {filteredOrders.length === 0 ? (
        <div className={styles.empty}>
          <h3>No orders found</h3>
        </div>
      ) : (
        <>
          <table className={styles.table}>
            <thead>
              <tr>
                <th>ID</th>
                <th>Customer</th>
                <th>Method</th>
                <th>Total</th>
                <th>Payment</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {filteredOrders.map((order) => (
                <>
                  <tr
                    key={order.id}
                    className={styles.orderRow}
                    onClick={() =>
                      setExpandedId(expandedId === order.id ? null : order.id)
                    }
                  >
                    <td>#{order.id}</td>
                    <td>{order.customerName}</td>
                    <td>
                      {order.deliveryMethod === "DELIVERY"
                        ? "Delivery"
                        : "Pickup"}
                    </td>
                    <td>{order.totalAmount}₴</td>
                    <td>
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
                    </td>
                    <td>
                      <span
                        className={styles.statusBadge}
                        style={{
                          background: ORDER_STATUS_COLORS[order.status],
                        }}
                      >
                        {ORDER_STATUS_LABELS[order.status]}
                      </span>
                    </td>
                    <td>
                      <div className={styles.actions}>
                        {STATUS_FLOW[order.status]?.map((nextStatus) => (
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
                            <span>Phone:</span>
                            <span>{order.phone}</span>
                          </div>
                          {order.address && (
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
                                <span>
                                  {item.productName} × {item.quantity}
                                </span>
                                <span>{item.price * item.quantity}₴</span>
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
