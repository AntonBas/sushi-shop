import { describe, it, expect } from 'vitest'
import { formatPrice } from './formatPrice'

describe('formatPrice', () => {
  it('rounds away the floating-point tail from price * quantity', () => {
    expect(formatPrice(79.99 * 3)).toBe('239.97')
  })

  it('pads a whole number to two decimals', () => {
    expect(formatPrice(80)).toBe('80.00')
  })

  it('rounds a sum of several imprecise floats without a tail', () => {
    const total = [0.1, 0.2, 0.3].reduce((sum, n) => sum + n, 0)
    expect(formatPrice(total)).toBe('0.60')
  })

  it('rounds to the nearest cent instead of truncating', () => {
    expect(formatPrice(19.995)).toBe('20.00')
    expect(formatPrice(19.994)).toBe('19.99')
  })
})
