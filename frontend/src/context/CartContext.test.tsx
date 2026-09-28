import { describe, it, expect, beforeEach } from 'vitest'
import { renderHook, act } from '@testing-library/react'
import type { ReactNode } from 'react'
import { CartProvider } from './CartContext'
import { useCart } from './useCart'
import { MAX_CART_QUANTITY } from './cart-context'

const wrapper = ({ children }: { children: ReactNode }) => <CartProvider>{children}</CartProvider>

const maki = { productId: 1, name: 'Maki', price: 100, quantity: 2 }

describe('CartContext', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  it('merges quantities of the same product and persists the cart', () => {
    const { result } = renderHook(() => useCart(), { wrapper })

    act(() => result.current.addItem(maki))
    act(() => result.current.addItem({ ...maki, quantity: 3 }))

    expect(result.current.items).toHaveLength(1)
    expect(result.current.items[0].quantity).toBe(5)
    expect(result.current.total).toBe(500)
    expect(JSON.parse(localStorage.getItem('cart') ?? '[]')).toHaveLength(1)
  })

  it('caps quantity at the backend limit', () => {
    const { result } = renderHook(() => useCart(), { wrapper })

    act(() => result.current.addItem({ ...maki, quantity: 90 }))
    act(() => result.current.addItem({ ...maki, quantity: 20 }))

    expect(result.current.items[0].quantity).toBe(MAX_CART_QUANTITY)
  })

  it('ignores a corrupted stored cart', () => {
    localStorage.setItem('cart', '{not json')

    const { result } = renderHook(() => useCart(), { wrapper })

    expect(result.current.items).toEqual([])
    expect(localStorage.getItem('cart')).toBeNull()
  })

  it('updates prices of known products and keeps the rest', () => {
    const { result } = renderHook(() => useCart(), { wrapper })
    const nigiri = { productId: 2, name: 'Nigiri', price: 80, quantity: 1 }

    act(() => result.current.addItem(maki))
    act(() => result.current.addItem(nigiri))
    act(() => result.current.updatePrices({ 1: 120 }))

    expect(result.current.items.map((i) => i.price)).toEqual([120, 80])
    expect(result.current.total).toBe(320)
    expect(JSON.parse(localStorage.getItem('cart') ?? '[]')).toEqual(result.current.items)
  })
})
