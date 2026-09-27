export type Category = 'ROLL' | 'SET' | 'DRINK' | 'DESSERT' | 'SOUP' | 'SALAD' | 'WOK' | 'EXTRA'

export type DeliveryMethod = 'DELIVERY' | 'PICKUP'

export type OrderStatus = 'NEW' | 'CONFIRMED' | 'COOKING' | 'DELIVERING' | 'READY' | 'DELIVERED' | 'CANCELLED'

export type PaymentMethod = 'ONLINE' | 'ON_DELIVERY'

export type UserRole = 'CUSTOMER' | 'ADMIN' | 'COURIER'

export type AuditAction = 'CREATE' | 'UPDATE' | 'DELETE'

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
  NEW: "#4f46e5",
  CONFIRMED: "#1d4ed8",
  COOKING: "#b45309",
  DELIVERING: "#6d28d9",
  READY: "#047857",
  DELIVERED: "#15803d",
  CANCELLED: "#b91c1c",
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

export const PAYMENT_STATUS_LABELS: Record<string, string> = {
  ON_DELIVERY: "On Delivery",
  PENDING: "Pending",
  PAID: "Paid",
  UNPAID: "Unpaid",
}