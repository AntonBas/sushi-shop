export type Category = 'ROLL' | 'SET' | 'DRINK' | 'DESSERT' | 'SOUP' | 'EXTRA'

export type DeliveryMethod = 'DELIVERY' | 'PICKUP'

export type OrderStatus = 'NEW' | 'CONFIRMED' | 'COOKING' | 'DELIVERING' | 'READY' | 'DELIVERED' | 'CANCELLED'

export type PaymentStatus = 'PENDING' | 'PAID' | 'FAILED' | 'REFUNDED'

export type UserRole = 'CUSTOMER' | 'ADMIN' | 'COURIER'

export const CATEGORY_DISPLAY: Record<Category, string> = {
  ROLL: 'Roll',
  SET: 'Set',
  DRINK: 'Drink',
  DESSERT: 'Dessert',
  SOUP: 'Soup',
  EXTRA: 'Extra',
}