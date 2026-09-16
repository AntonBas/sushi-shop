import { describe, it, expect, vi } from 'vitest'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { AxiosError, AxiosHeaders } from 'axios'
import Login from './Login'
import { useAuth } from '../../../context/useAuth'
import { useResendVerification } from '../../../hooks/common/useResendVerification'

vi.mock('../../../context/useAuth')
vi.mock('../../../hooks/common/useResendVerification')

const OAUTH_NO_PASSWORD_ERROR = 'This account uses Google sign-in and has no password set. Sign in with Google, or use "Forgot password?" to set one.'

function axiosErrorWithMessage(message: string) {
  return new AxiosError(
    'Request failed',
    '400',
    { headers: new AxiosHeaders() },
    {},
    {
      status: 400,
      statusText: 'Bad Request',
      headers: {},
      config: { headers: new AxiosHeaders() },
      data: { message },
    },
  )
}

function renderLogin() {
  return render(
    <MemoryRouter>
      <Login />
    </MemoryRouter>,
  )
}

describe('Login', () => {
  it('shows a Google sign-in prompt when the account has no password set', async () => {
    vi.mocked(useAuth).mockReturnValue({
      login: vi.fn().mockRejectedValue(axiosErrorWithMessage(OAUTH_NO_PASSWORD_ERROR)),
    } as unknown as ReturnType<typeof useAuth>)
    vi.mocked(useResendVerification).mockReturnValue({
      resend: vi.fn(),
      syncStatus: vi.fn(),
      cooldown: 0,
      sending: false,
      message: '',
      messageType: null,
    } as unknown as ReturnType<typeof useResendVerification>)

    renderLogin()

    fireEvent.change(screen.getByPlaceholderText('your@email.com'), { target: { value: 'anton@example.com' } })
    fireEvent.change(screen.getByPlaceholderText('••••••••'), { target: { value: 'password123' } })
    fireEvent.click(screen.getByRole('button', { name: 'Login' }))

    await waitFor(() => expect(screen.getByText(OAUTH_NO_PASSWORD_ERROR)).toBeInTheDocument())

    expect(screen.getAllByRole('button', { name: 'Continue with Google' })).toHaveLength(2)
    expect(screen.getByRole('link', { name: 'set a password' })).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /Resend verification email/ })).not.toBeInTheDocument()
  })
})
