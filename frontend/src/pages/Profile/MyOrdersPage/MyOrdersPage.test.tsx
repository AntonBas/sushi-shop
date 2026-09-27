import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, fireEvent } from '@testing-library/react'
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
})
