import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import CheckoutPage from './CheckoutPage'
import { useAuth } from '../../context/useAuth'
import { useCart } from '../../context/useCart'
import { useOrders } from '../../hooks/features/useOrders'
import { useNotification } from '../../context/useNotification'
import * as paymentsApi from '../../api/payments'
import type { UserResponse } from '../../types'

vi.mock('../../context/useAuth')
vi.mock('../../context/useCart')
vi.mock('../../hooks/features/useOrders')
vi.mock('../../context/useNotification')
vi.mock('../../api/payments')

const createOrder = vi.fn()
const clearCart = vi.fn()
const showNotification = vi.fn()

const cartItem = { productId: 7, name: 'Maki', price: 250, quantity: 2 }

function mockCart(items: typeof cartItem[]) {
  vi.mocked(useCart).mockReturnValue({
    items,
    total: items.reduce((sum, i) => sum + i.price * i.quantity, 0),
    clearCart,
  } as unknown as ReturnType<typeof useCart>)
}

function mockUser(user: Partial<UserResponse> | null) {
  vi.mocked(useAuth).mockReturnValue({ user } as unknown as ReturnType<typeof useAuth>)
}

function renderCheckout() {
  return render(
    <MemoryRouter initialEntries={['/checkout']}>
      <Routes>
        <Route path="/checkout" element={<CheckoutPage />} />
        <Route path="/profile/orders" element={<div>My orders</div>} />
      </Routes>
    </MemoryRouter>,
  )
}

describe('CheckoutPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockCart([cartItem])
    mockUser({ name: 'Anton', phone: '+380961791111', address: undefined })
    vi.mocked(useOrders).mockReturnValue({ createOrder, loading: false } as unknown as ReturnType<typeof useOrders>)
    vi.mocked(useNotification).mockReturnValue({ showNotification } as unknown as ReturnType<typeof useNotification>)
  })

  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('shows an empty state when the cart is empty', () => {
    mockCart([])

    renderCheckout()

    expect(screen.getByText('Your cart is empty')).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Place Order' })).not.toBeInTheDocument()
  })

  it('prefills contact info and switches to delivery when the user has a saved address', () => {
    mockUser({
      name: 'Anton',
      phone: '+380961791111',
      address: { city: 'Lviv', street: 'Zelena', house: '204', apartment: '280' },
    })

    renderCheckout()

    expect(screen.getByLabelText('Name')).toHaveValue('Anton')
    expect(screen.getByLabelText('City')).toHaveValue('Lviv')
    expect(screen.getByRole('button', { name: 'Delivery' })).toHaveAttribute('aria-pressed', 'true')
    expect(screen.getByRole('button', { name: 'Pickup' })).toHaveAttribute('aria-pressed', 'false')
  })

  it('places a pickup order without an address and navigates to my orders', async () => {
    createOrder.mockResolvedValue({ id: 42 })

    renderCheckout()
    fireEvent.click(screen.getByRole('button', { name: 'Place Order' }))

    expect(await screen.findByText('My orders')).toBeInTheDocument()
    expect(createOrder).toHaveBeenCalledWith({
      customerName: 'Anton',
      phone: '+380961791111',
      paymentMethod: 'ON_DELIVERY',
      deliveryMethod: 'PICKUP',
      address: undefined,
      items: [{ productId: 7, quantity: 2 }],
    })
    expect(clearCart).toHaveBeenCalled()
    expect(showNotification).toHaveBeenCalledWith('Order placed successfully!', 'success')
  })

  it('redirects to Stripe checkout for online payment', async () => {
    const assign = vi.fn()
    vi.stubGlobal('location', { assign })
    createOrder.mockResolvedValue({ id: 42 })
    vi.mocked(paymentsApi.createCheckout).mockResolvedValue('https://checkout.stripe.com/pay/cs_1')

    renderCheckout()
    fireEvent.click(screen.getByRole('button', { name: 'Pay Online' }))
    expect(screen.getByRole('button', { name: 'Pay Online' })).toHaveAttribute('aria-pressed', 'true')
    fireEvent.click(screen.getByRole('button', { name: 'Proceed to Payment' }))

    await waitFor(() => expect(assign).toHaveBeenCalledWith('https://checkout.stripe.com/pay/cs_1'))
    expect(paymentsApi.createCheckout).toHaveBeenCalledWith(42)
    expect(clearCart).toHaveBeenCalled()
  })

  it('navigates to my orders when creating the payment session fails', async () => {
    createOrder.mockResolvedValue({ id: 42 })
    vi.mocked(paymentsApi.createCheckout).mockRejectedValue(new Error('Stripe down'))

    renderCheckout()
    fireEvent.click(screen.getByRole('button', { name: 'Pay Online' }))
    fireEvent.click(screen.getByRole('button', { name: 'Proceed to Payment' }))

    expect(await screen.findByText('My orders')).toBeInTheDocument()
    expect(showNotification).toHaveBeenCalledWith(expect.any(String), 'error')
  })

  it('stays on the page and keeps the cart when order creation fails', async () => {
    createOrder.mockRejectedValue(new Error('Validation failed'))

    renderCheckout()
    fireEvent.click(screen.getByRole('button', { name: 'Place Order' }))

    await waitFor(() => expect(createOrder).toHaveBeenCalled())
    expect(clearCart).not.toHaveBeenCalled()
    expect(screen.getByRole('button', { name: 'Place Order' })).toBeInTheDocument()
  })
})
