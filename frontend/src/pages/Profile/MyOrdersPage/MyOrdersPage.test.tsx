import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, fireEvent, act, waitFor } from '@testing-library/react'
import type { Client, IMessage } from '@stomp/stompjs'
import MyOrdersPage from './MyOrdersPage'
import { useNotification } from '../../../context/useNotification'
import { useOrderSocket } from '../../../hooks/features/useOrderSocket'
import { getMyOrders } from '../../../api/orders'
import type { UserOrderResponse } from '../../../types'

vi.mock('../../../context/useNotification')
vi.mock('../../../hooks/features/useOrderSocket')
vi.mock('../../../api/orders')

const order: UserOrderResponse = {
  id: 7,
  userEmail: 'anton@example.com',
  status: 'NEW',
  deliveryMethod: 'PICKUP',
  paymentMethod: 'ON_DELIVERY',
  paymentStatus: 'ON_DELIVERY',
  totalAmount: 500,
  createdAt: '2026-09-27T10:00:00',
  items: [{ productId: 1, productName: 'Maki', quantity: 2, unitPrice: 250 }],
}

describe('MyOrdersPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(useNotification).mockReturnValue({ showNotification: vi.fn() } as unknown as ReturnType<typeof useNotification>)
    vi.mocked(useOrderSocket).mockReturnValue({ current: null })
    vi.mocked(getMyOrders).mockResolvedValue({
      content: [order],
      page: { size: 12, number: 0, totalElements: 1, totalPages: 1 },
    })
  })

  it('expands order details from the keyboard', async () => {
    render(<MyOrdersPage />)
    const header = await screen.findByRole('button', { name: /Order #7/ })

    expect(header).toHaveAttribute('tabindex', '0')
    expect(header).toHaveAttribute('aria-expanded', 'false')
    expect(screen.queryByText(/Maki × 2/)).not.toBeInTheDocument()

    fireEvent.keyDown(header, { key: 'Enter' })
    expect(header).toHaveAttribute('aria-expanded', 'true')
    expect(screen.getByText(/Maki × 2/)).toBeInTheDocument()

    fireEvent.keyDown(header, { key: ' ' })
    expect(header).toHaveAttribute('aria-expanded', 'false')
    expect(screen.queryByText(/Maki × 2/)).not.toBeInTheDocument()
  })

  it('ignores other keys', async () => {
    render(<MyOrdersPage />)
    const header = await screen.findByRole('button', { name: /Order #7/ })

    fireEvent.keyDown(header, { key: 'a' })

    expect(header).toHaveAttribute('aria-expanded', 'false')
  })

  it('updates order and payment status from a live socket message', async () => {
    let onSocketConnect: ((client: Client) => void) | undefined
    vi.mocked(useOrderSocket).mockImplementation((callback) => {
      onSocketConnect = callback
      return { current: null }
    })
    const handlers = new Map<string, (message: IMessage) => void>()
    const client = {
      connected: true,
      subscribe: (destination: string, handler: (message: IMessage) => void) => {
        handlers.set(destination, handler)
        return { unsubscribe: vi.fn() }
      },
    } as unknown as Client
    vi.mocked(getMyOrders).mockResolvedValue({
      content: [{ ...order, paymentMethod: 'ONLINE', paymentStatus: 'PENDING' }],
      page: { size: 12, number: 0, totalElements: 1, totalPages: 1 },
    })

    render(<MyOrdersPage />)
    await screen.findByRole('button', { name: /Order #7/ })
    expect(screen.getByText('Pending')).toBeInTheDocument()

    await waitFor(() => {
      act(() => onSocketConnect?.(client))
      expect(handlers.has('/topic/orders/7')).toBe(true)
    })
    const handler = handlers.get('/topic/orders/7')
    act(() => handler?.({
      body: JSON.stringify({ orderId: 7, status: 'CONFIRMED', paymentStatus: 'PAID' }),
    } as IMessage))

    expect(screen.getByText('Paid')).toBeInTheDocument()
    expect(screen.getByText('Confirmed')).toBeInTheDocument()
    expect(screen.queryByText('Pending')).not.toBeInTheDocument()
  })
})
