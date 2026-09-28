import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import GuestRoute from './GuestRoute'
import { useAuth } from '../../context/useAuth'

vi.mock('../../context/useAuth')

function renderLogin(isAuthenticated: boolean) {
  vi.mocked(useAuth).mockReturnValue({ isAuthenticated } as ReturnType<typeof useAuth>)
  return render(
    <MemoryRouter initialEntries={['/login']}>
      <Routes>
        <Route path="/" element={<p>Home</p>} />
        <Route path="/login" element={<GuestRoute><p>Login form</p></GuestRoute>} />
      </Routes>
    </MemoryRouter>,
  )
}

describe('GuestRoute', () => {
  it('shows the page to guests', () => {
    renderLogin(false)
    expect(screen.getByText('Login form')).toBeInTheDocument()
  })

  it('redirects signed-in users home', () => {
    renderLogin(true)
    expect(screen.getByText('Home')).toBeInTheDocument()
  })
})
