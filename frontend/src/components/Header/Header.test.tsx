import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, fireEvent } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import Header from './Header'
import { useAuth } from '../../context/useAuth'
import { useCart } from '../../context/useCart'

vi.mock('../../context/useAuth')
vi.mock('../../context/useCart')

function clickLikeUser(element: Element) {
  fireEvent.mouseDown(element)
  fireEvent.click(element)
}

describe('Header mobile menu', () => {
  beforeEach(() => {
    vi.mocked(useAuth).mockReturnValue({
      user: null,
      isAuthenticated: false,
      isAdmin: false,
      isCourier: false,
      logout: vi.fn(),
    } as unknown as ReturnType<typeof useAuth>)
    vi.mocked(useCart).mockReturnValue({ count: 0 } as unknown as ReturnType<typeof useCart>)
  })

  it('opens and closes with the hamburger button', () => {
    render(<MemoryRouter><Header /></MemoryRouter>)
    const toggle = screen.getByRole('button', { name: 'Toggle menu' })

    clickLikeUser(toggle)
    expect(toggle).toHaveAttribute('aria-expanded', 'true')

    clickLikeUser(toggle)
    expect(toggle).toHaveAttribute('aria-expanded', 'false')
  })

  it('closes when clicking outside the menu', () => {
    render(<MemoryRouter><Header /></MemoryRouter>)
    const toggle = screen.getByRole('button', { name: 'Toggle menu' })
    clickLikeUser(toggle)

    fireEvent.mouseDown(document.body)

    expect(toggle).toHaveAttribute('aria-expanded', 'false')
  })
})
