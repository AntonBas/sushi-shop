import type { AddressRequest, AddressResponse } from "./address"
import type { DeliveryMethod, OrderStatus, PaymentStatus } from "./enums"

export interface CreateOrderRequest {
  customerName: string
  phone: string
  deliveryMethod: DeliveryMethod
  address?: AddressRequest
  items: OrderItemRequest[]
}

export interface OrderItemRequest {
  productId: number
  quantity: number
}

export interface OrderResponse {
  id: number
  customerName: string
  userEmail: string
  phone: string
  address?: AddressResponse
  deliveryMethod: DeliveryMethod
  status: OrderStatus
  paymentStatus: PaymentStatus
  totalAmount: number
  createdAt: string
  items: OrderItemResponse[]
}

export interface UserOrderResponse {
  id: number
  deliveryMethod: DeliveryMethod
  status: OrderStatus
  paymentStatus: PaymentStatus
  totalAmount: number
  createdAt: string
  items: OrderItemResponse[]
}

export interface OrderItemResponse {
  productId: number
  productName: string
  quantity: number
  price: number
  mainImage?: string | null
}

export interface OrderStatusUpdateResponse {
  orderId: number
  status: OrderStatus
}