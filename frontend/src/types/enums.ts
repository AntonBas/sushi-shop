export type Category = 'ROLL' | 'SET' | 'DRINK' | 'DESSERT' | 'SOUP' | 'SALAD' | 'WOK' | 'EXTRA'

export type DeliveryMethod = 'DELIVERY' | 'PICKUP'

export type OrderStatus = 'NEW' | 'CONFIRMED' | 'COOKING' | 'DELIVERING' | 'READY' | 'DELIVERED' | 'CANCELLED'

export type PaymentStatus = 'PENDING' | 'PAID' | 'FAILED' | 'REFUNDED'

export type UserRole = 'CUSTOMER' | 'ADMIN' | 'COURIER'

export type AuditAction = 'CREATE' | 'UPDATE' | 'DELETE' | 'LOGIN' | 'LOGOUT' | 'EXPORT'

export const CATEGORY_DISPLAY: Record<Category, string> = {
  ROLL: 'Roll',
  SET: 'Set',
  DRINK: 'Drink',
  DESSERT: 'Dessert',
  SOUP: 'Soup',
  SALAD: 'Salad',
  WOK: 'Wok',
  EXTRA: 'Extra',
}

export const ORDER_STATUS_COLORS: Record<OrderStatus, string> = {
  NEW: "#6366f1",
  CONFIRMED: "#3b82f6",
  COOKING: "#f59e0b",
  DELIVERING: "#8b5cf6",
  READY: "#10b981",
  DELIVERED: "#22c55e",
  CANCELLED: "#ef4444",
}

export const ORDER_STATUS_LABELS: Record<OrderStatus, string> = {
  NEW: "New",
  CONFIRMED: "Confirmed",
  COOKING: "Cooking",
  DELIVERING: "Delivering",
  READY: "Ready",
  DELIVERED: "Delivered",
  CANCELLED: "Cancelled",
}

export const PAYMENT_STATUS_COLORS: Record<PaymentStatus, string> = {
  PENDING: "#f59e0b",
  PAID: "#22c55e",
  FAILED: "#ef4444",
  REFUNDED: "#6366f1",
}

export const PAYMENT_STATUS_LABELS: Record<PaymentStatus, string> = {
  PENDING: "Payment Pending",
  PAID: "Paid",
  FAILED: "Payment Failed",
  REFUNDED: "Refunded",
}