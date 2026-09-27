import { describe, it, expect, vi, afterEach } from 'vitest'
import type { ReactNode } from 'react'
import { renderHook, act } from '@testing-library/react'
import { MemoryRouter, useLocation } from 'react-router-dom'
import { pickAllowed, useDebouncedParamInput, useListSearchParams } from './useListSearchParams'

function wrapperAt(url: string) {
  return function Wrapper({ children }: { children: ReactNode }) {
    return <MemoryRouter initialEntries={[url]}>{children}</MemoryRouter>
  }
}

function useListWithLocation() {
  return { ...useListSearchParams(), location: useLocation() }
}

describe('useListSearchParams', () => {
  it('reads a one-based page from the URL as a zero-based index', () => {
    const { result } = renderHook(() => useListSearchParams(), { wrapper: wrapperAt('/list?page=3') })

    expect(result.current.page).toBe(2)
  })

  it.each(['abc', '0', '-2', '1.5'])('treats an invalid page %s as the first page', (value) => {
    const { result } = renderHook(() => useListSearchParams(), { wrapper: wrapperAt(`/list?page=${value}`) })

    expect(result.current.page).toBe(0)
  })

  it('writes the page to the URL and omits it for the first page', () => {
    const { result } = renderHook(() => useListWithLocation(), { wrapper: wrapperAt('/list?status=NEW') })

    act(() => result.current.setPage(1))
    expect(result.current.location.search).toBe('?status=NEW&page=2')

    act(() => result.current.setPage(0))
    expect(result.current.location.search).toBe('?status=NEW')
  })

  it('resets the page and drops empty values when a filter changes', () => {
    const { result } = renderHook(() => useListWithLocation(), { wrapper: wrapperAt('/list?status=NEW&search=anton&page=4') })

    act(() => result.current.updateParams({ status: 'COOKING', search: '' }))

    expect(result.current.location.search).toBe('?status=COOKING')
    expect(result.current.page).toBe(0)
  })
})

describe('pickAllowed', () => {
  it('keeps allowed values and drops unknown ones', () => {
    expect(pickAllowed('NEW', ['NEW', 'CANCELLED'] as const)).toBe('NEW')
    expect(pickAllowed('user.phone', ['NEW', 'CANCELLED'] as const)).toBe('')
  })
})

describe('useDebouncedParamInput', () => {
  afterEach(() => {
    vi.useRealTimers()
  })

  it('commits the typed value after the debounce delay', () => {
    vi.useFakeTimers()
    const commit = vi.fn()
    const { result } = renderHook(() => useDebouncedParamInput('', commit))

    act(() => result.current[1]('mak'))
    act(() => result.current[1]('maki'))
    expect(result.current[0]).toBe('maki')
    expect(commit).not.toHaveBeenCalled()

    act(() => {
      vi.advanceTimersByTime(300)
    })
    expect(commit).toHaveBeenCalledTimes(1)
    expect(commit).toHaveBeenCalledWith('maki')
  })

  it('does not commit after unmount', () => {
    vi.useFakeTimers()
    const commit = vi.fn()
    const { result, unmount } = renderHook(() => useDebouncedParamInput('', commit))

    act(() => result.current[1]('maki'))
    unmount()
    vi.advanceTimersByTime(300)

    expect(commit).not.toHaveBeenCalled()
  })

  it('follows the URL value when it changes from outside', () => {
    const { result, rerender } = renderHook(({ value }) => useDebouncedParamInput(value, vi.fn()), {
      initialProps: { value: 'maki' },
    })

    rerender({ value: '' })

    expect(result.current[0]).toBe('')
  })
})
