import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { render, waitFor } from '@testing-library/react'
import { MemoryRouter, Route, Routes, useLocation } from 'react-router-dom'
import OAuth2Redirect from './OAuth2Redirect'
import { useAuth } from '../../../context/useAuth'
import { exchangeOAuth2Code } from '../../../api/auth'

vi.mock('../../../context/useAuth')
vi.mock('../../../api/auth')

function LocationProbe() {
  const location = useLocation()
  return <div data-testid="location">{location.pathname + location.search}</div>
}

function renderRedirect() {
  return render(
    <MemoryRouter initialEntries={['/oauth2/redirect']}>
      <Routes>
        <Route path="/oauth2/redirect" element={<OAuth2Redirect />} />
        <Route path="/login" element={<LocationProbe />} />
      </Routes>
    </MemoryRouter>,
  )
}

function stubLocation(search: string) {
  const location = { search, pathname: '/oauth2/redirect', href: '' }
  vi.stubGlobal('location', location)
  return location
}

describe('OAuth2Redirect', () => {
  const refreshUser = vi.fn()

  beforeEach(() => {
    vi.clearAllMocks()
    refreshUser.mockReset()
    vi.mocked(useAuth).mockReturnValue({ refreshUser } as unknown as ReturnType<typeof useAuth>)
    vi.spyOn(window.history, 'replaceState').mockImplementation(() => {})
  })

  afterEach(() => {
    vi.unstubAllGlobals()
    vi.restoreAllMocks()
  })

  it('exchanges the code once even if auth callbacks change identity, then reloads to home', async () => {
    const location = stubLocation('?code=abc')
    vi.mocked(exchangeOAuth2Code).mockResolvedValue({} as Awaited<ReturnType<typeof exchangeOAuth2Code>>)
    refreshUser.mockResolvedValue(undefined)

    vi.mocked(useAuth).mockImplementation(
      () => ({ refreshUser: () => refreshUser() }) as unknown as ReturnType<typeof useAuth>,
    )

    const { rerender } = renderRedirect()
    rerender(
      <MemoryRouter initialEntries={['/oauth2/redirect']}>
        <Routes>
          <Route path="/oauth2/redirect" element={<OAuth2Redirect />} />
          <Route path="/login" element={<LocationProbe />} />
        </Routes>
      </MemoryRouter>,
    )

    await waitFor(() => expect(location.href).toBe('/'))
    expect(exchangeOAuth2Code).toHaveBeenCalledTimes(1)
    expect(exchangeOAuth2Code).toHaveBeenCalledWith('abc')
    expect(refreshUser).toHaveBeenCalledTimes(1)
    expect(window.history.replaceState).toHaveBeenCalledWith(null, '', '/oauth2/redirect')
  })

  it('redirects to login with an error when the code is missing', async () => {
    stubLocation('')

    const { findByTestId } = renderRedirect()

    expect((await findByTestId('location')).textContent).toBe('/login?error=oauth2_failed')
    expect(exchangeOAuth2Code).not.toHaveBeenCalled()
  })

  it('redirects to login with an error when the exchange fails', async () => {
    const location = stubLocation('?code=expired')
    vi.mocked(exchangeOAuth2Code).mockRejectedValue(new Error('expired'))

    const { findByTestId } = renderRedirect()

    expect((await findByTestId('location')).textContent).toBe('/login?error=oauth2_failed')
    expect(refreshUser).not.toHaveBeenCalled()
    expect(location.href).toBe('')
  })
})
