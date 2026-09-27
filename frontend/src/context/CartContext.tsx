import { useState, useCallback, type ReactNode } from "react";
import { CartContext, MAX_CART_QUANTITY, type CartItem } from "./cart-context";

function loadStoredCart(): CartItem[] {
  const saved = localStorage.getItem("cart");
  if (!saved) return [];
  try {
    const parsed: unknown = JSON.parse(saved);
    return Array.isArray(parsed) ? (parsed as CartItem[]) : [];
  } catch {
    localStorage.removeItem("cart");
    return [];
  }
}

export function CartProvider({ children }: { children: ReactNode }) {
  const [items, setItems] = useState<CartItem[]>(loadStoredCart);

  const addItem = useCallback((item: CartItem) => {
    setItems((prev) => {
      const existing = prev.find((i) => i.productId === item.productId);
      const updated = existing
        ? prev.map((i) =>
            i.productId === item.productId
              ? { ...i, quantity: Math.min(MAX_CART_QUANTITY, i.quantity + item.quantity) }
              : i,
          )
        : [...prev, { ...item, quantity: Math.min(MAX_CART_QUANTITY, item.quantity) }];
      localStorage.setItem("cart", JSON.stringify(updated));
      return updated;
    });
  }, []);

  const removeItem = useCallback((productId: number) => {
    setItems((prev) => {
      const updated = prev.filter((i) => i.productId !== productId);
      localStorage.setItem("cart", JSON.stringify(updated));
      return updated;
    });
  }, []);

  const clearCart = useCallback(() => {
    setItems([]);
    localStorage.removeItem("cart");
  }, []);

  const updatePrices = useCallback((prices: Record<number, number>) => {
    setItems((prev) => {
      const updated = prev.map((i) =>
        prices[i.productId] !== undefined ? { ...i, price: prices[i.productId] } : i,
      );
      localStorage.setItem("cart", JSON.stringify(updated));
      return updated;
    });
  }, []);

  const total = items.reduce((sum, i) => sum + i.price * i.quantity, 0);
  const count = items.reduce((sum, i) => sum + i.quantity, 0);

  return (
    <CartContext.Provider
      value={{ items, addItem, removeItem, clearCart, updatePrices, total, count }}
    >
      {children}
    </CartContext.Provider>
  );
}
