import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import ProtectedRoute from './ProtectedRoute'
import { useAuth } from '../../context/useAuth'

vi.mock('../../context/useAuth')

function mockAuth(overrides: Partial<ReturnType<typeof useAuth>>) {
  vi.mocked(useAuth).mockReturnValue({
    isAuthenticated: true,
    isAdmin: false,
    isCourier: false,
    loading: false,
    ...overrides,
  } as ReturnType<typeof useAuth>)
}

function renderRoute(props: { adminOnly?: boolean; staffOnly?: boolean }) {
  return render(
    <MemoryRouter initialEntries={['/protected']}>
      <Routes>
        <Route path="/" element={<p>Home</p>} />
        <Route path="/login" element={<p>Login page</p>} />
        <Route
          path="/protected"
          element={
            <ProtectedRoute {...props}>
              <p>Secret</p>
            </ProtectedRoute>
          }
        />
      </Routes>
    </MemoryRouter>,
  )
}

describe('ProtectedRoute', () => {
  it('redirects guests to login', () => {
    mockAuth({ isAuthenticated: false })
    renderRoute({})
    expect(screen.getByText('Login page')).toBeInTheDocument()
  })

  it('renders content for an authenticated user', () => {
    mockAuth({})
    renderRoute({})
    expect(screen.getByText('Secret')).toBeInTheDocument()
  })

  it('keeps couriers out of admin-only routes', () => {
    mockAuth({ isCourier: true })
    renderRoute({ adminOnly: true })
    expect(screen.getByText('Home')).toBeInTheDocument()
  })

  it('lets couriers into staff routes', () => {
    mockAuth({ isCourier: true })
    renderRoute({ staffOnly: true })
    expect(screen.getByText('Secret')).toBeInTheDocument()
  })

  it('keeps customers out of staff routes', () => {
    mockAuth({})
    renderRoute({ staffOnly: true })
    expect(screen.getByText('Home')).toBeInTheDocument()
  })
})
