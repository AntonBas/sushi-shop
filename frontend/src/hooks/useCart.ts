import { useState, useCallback } from 'react'
import type { OrderItemRequest } from '../types'

interface CartItem extends OrderItemRequest {
  name: string
  price: number
}

export function useCart() {
  const [items, setItems] = useState<CartItem[]>(() => {
    const saved = localStorage.getItem('cart')
    return saved ? JSON.parse(saved) : []
  })

  const addItem = useCallback((item: CartItem) => {
    setItems(prev => {
      const existing = prev.find(i => i.productId === item.productId)
      const updated = existing
        ? prev.map(i => i.productId === item.productId ? { ...i, quantity: i.quantity + item.quantity } : i)
        : [...prev, item]
      localStorage.setItem('cart', JSON.stringify(updated))
      return updated
    })
  }, [])

  const removeItem = useCallback((productId: number) => {
    setItems(prev => {
      const updated = prev.filter(i => i.productId !== productId)
      localStorage.setItem('cart', JSON.stringify(updated))
      return updated
    })
  }, [])

  const clearCart = useCallback(() => {
    setItems([])
    localStorage.removeItem('cart')
  }, [])

  const total = items.reduce((sum, i) => sum + i.price * i.quantity, 0)
  const count = items.reduce((sum, i) => sum + i.quantity, 0)

  return { items, addItem, removeItem, clearCart, total, count }
}