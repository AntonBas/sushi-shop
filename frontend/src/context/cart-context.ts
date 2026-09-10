import { createContext } from "react";

export interface CartItem {
  productId: number;
  name: string;
  price: number;
  quantity: number;
  mainImage?: string | null;
}

export interface CartContextType {
  items: CartItem[];
  addItem: (item: CartItem) => void;
  removeItem: (productId: number) => void;
  clearCart: () => void;
  total: number;
  count: number;
}

export const CartContext = createContext<CartContextType | undefined>(undefined);
