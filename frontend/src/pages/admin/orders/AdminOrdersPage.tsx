import { useState } from 'react'
import { useAdminOrders } from '../../../hooks/features/useAdmin'
import { useNotification } from '../../../context/NotificationContext'
import * as ordersApi from '../../../api/orders'
import Loading from '../../../components/ui/Loading/Loading'
import Pagination from '../../../components/ui/Pagination/Pagination'
import type { OrderStatus } from '../../../types'
import styles from './AdminOrdersPage.module.css'

const STATUS_FLOW: Record<OrderStatus, OrderStatus[]> = {
  NEW: ['CONFIRMED', 'CANCELLED'],
  CONFIRMED: ['COOKING', 'CANCELLED'],
  COOKING: ['DELIVERING', 'READY'],
  DELIVERING: ['DELIVERED'],
  READY: ['DELIVERED'],
  DELIVERED: [],
  CANCELLED: [],
}

const STATUS_COLORS: Record<OrderStatus, string> = {
  NEW: '#6366f1',
  CONFIRMED: '#3b82f6',
  COOKING: '#f59e0b',
  DELIVERING: '#8b5cf6',
  READY: '#10b981',
  DELIVERED: '#22c55e',
  CANCELLED: '#ef4444',
}

export default function AdminOrdersPage() {
  const { orders, totalPages, loading, loadOrders } = useAdminOrders()
  const { showNotification } = useNotification()
  const [page, setPage] = useState(0)
  const [statusFilter, setStatusFilter] = useState<OrderStatus | ''>('')

  const handleStatusChange = async (orderId: number, newStatus: OrderStatus) => {
    try {
      await ordersApi.updateOrderStatus(orderId, newStatus)
      showNotification(`Order #${orderId} → ${newStatus}`, 'success')
      loadOrders(page)
    } catch {
      showNotification('Failed to update status', 'error')
    }
  }

  const filteredOrders = statusFilter ? orders.filter((o) => o.status === statusFilter) : orders

  if (loading) return <Loading text="Loading orders..." />

  return (
    <div className={styles.page}>
      <div className={styles.header}>
        <h1>Orders</h1>
      </div>

      <div className={styles.filters}>
        <select
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value as OrderStatus | '')}
          className={styles.filterSelect}
        >
          <option value="">All Statuses</option>
          {Object.keys(STATUS_FLOW).map((s) => (
            <option key={s} value={s}>{s}</option>
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
                <th>Phone</th>
                <th>Method</th>
                <th>Total</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {filteredOrders.map((order) => (
                <tr key={order.id}>
                  <td>#{order.id}</td>
                  <td>{order.customerName}</td>
                  <td>{order.phone}</td>
                  <td>{order.deliveryMethod}</td>
                  <td>₴{order.totalAmount}</td>
                  <td>
                    <span
                      className={styles.statusBadge}
                      style={{ background: STATUS_COLORS[order.status] }}
                    >
                      {order.status}
                    </span>
                  </td>
                  <td>
                    <div className={styles.actions}>
                      {STATUS_FLOW[order.status]?.map((nextStatus) => (
                        <button
                          key={nextStatus}
                          onClick={() => handleStatusChange(order.id, nextStatus)}
                          className={styles.actionBtn}
                          style={{ background: STATUS_COLORS[nextStatus] }}
                        >
                          {nextStatus}
                        </button>
                      ))}
                    </div>
                  </td>
                </tr>
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
  )
}