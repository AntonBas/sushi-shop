import type { OrderResponse, OrderStatus } from "../types";

export const getStatusFlow = (
  order: Pick<OrderResponse, "status" | "deliveryMethod" | "paymentMethod" | "paymentStatus">,
): OrderStatus[] => {
  const cashOnDelivery = order.paymentMethod === "ON_DELIVERY";
  switch (order.status) {
    case "NEW":
      return order.paymentMethod === "ONLINE" && order.paymentStatus !== "PAID"
        ? ["CANCELLED"]
        : ["CONFIRMED", "CANCELLED"];
    case "CONFIRMED":
      return cashOnDelivery ? ["COOKING", "CANCELLED"] : ["COOKING"];
    case "COOKING":
      return order.deliveryMethod === "DELIVERY" ? ["DELIVERING"] : ["READY"];
    case "DELIVERING":
    case "READY":
      return ["DELIVERED"];
    default:
      return [];
  }
};
