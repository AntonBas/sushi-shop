import { describe, it, expect } from 'vitest'
import { getStatusFlow } from './orderStatusFlow'

describe('getStatusFlow', () => {
  it('offers only cancellation for an unpaid online order', () => {
    expect(getStatusFlow({ status: 'NEW', deliveryMethod: 'DELIVERY', paymentMethod: 'ONLINE', paymentStatus: 'PENDING' }))
      .toEqual(['CANCELLED'])
  })

  it('offers confirmation for a paid online order', () => {
    expect(getStatusFlow({ status: 'NEW', deliveryMethod: 'DELIVERY', paymentMethod: 'ONLINE', paymentStatus: 'PAID' }))
      .toEqual(['CONFIRMED', 'CANCELLED'])
  })

  it('allows cancelling a confirmed cash-on-delivery order', () => {
    expect(getStatusFlow({ status: 'CONFIRMED', deliveryMethod: 'PICKUP', paymentMethod: 'ON_DELIVERY', paymentStatus: 'ON_DELIVERY' }))
      .toEqual(['COOKING', 'CANCELLED'])
  })

  it('does not allow cancelling a confirmed online order', () => {
    expect(getStatusFlow({ status: 'CONFIRMED', deliveryMethod: 'DELIVERY', paymentMethod: 'ONLINE', paymentStatus: 'PAID' }))
      .toEqual(['COOKING'])
  })

  it('routes cooking orders by delivery method', () => {
    expect(getStatusFlow({ status: 'COOKING', deliveryMethod: 'DELIVERY', paymentMethod: 'ONLINE', paymentStatus: 'PAID' }))
      .toEqual(['DELIVERING'])
    expect(getStatusFlow({ status: 'COOKING', deliveryMethod: 'PICKUP', paymentMethod: 'ONLINE', paymentStatus: 'PAID' }))
      .toEqual(['READY'])
  })
})
