import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { renderHook, act } from '@testing-library/react'
import { useDelayedLoading } from './useDelayedLoading'

describe('useDelayedLoading', () => {
  beforeEach(() => {
    vi.useFakeTimers()
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  it('does not show loading before the delay has elapsed', () => {
    const { result } = renderHook(() => useDelayedLoading(true, { delay: 150, minDisplayTime: 300 }))
    expect(result.current).toBe(false)

    act(() => vi.advanceTimersByTime(100))
    expect(result.current).toBe(false)
  })

  it('shows loading once the delay has elapsed', () => {
    const { result } = renderHook(() => useDelayedLoading(true, { delay: 150, minDisplayTime: 300 }))

    act(() => vi.advanceTimersByTime(150))
    expect(result.current).toBe(true)
  })

  it('never shows loading if isLoading becomes false before the delay', () => {
    const { result, rerender } = renderHook(
      ({ isLoading }) => useDelayedLoading(isLoading, { delay: 150, minDisplayTime: 300 }),
      { initialProps: { isLoading: true } }
    )

    act(() => vi.advanceTimersByTime(100))
    rerender({ isLoading: false })
    act(() => vi.advanceTimersByTime(100))

    expect(result.current).toBe(false)
  })

  it('keeps loading visible for at least minDisplayTime once shown', () => {
    const { result, rerender } = renderHook(
      ({ isLoading }) => useDelayedLoading(isLoading, { delay: 150, minDisplayTime: 300 }),
      { initialProps: { isLoading: true } }
    )

    act(() => vi.advanceTimersByTime(150))
    expect(result.current).toBe(true)

    rerender({ isLoading: false })
    act(() => vi.advanceTimersByTime(200))
    expect(result.current).toBe(true)

    act(() => vi.advanceTimersByTime(100))
    expect(result.current).toBe(false)
  })
})
