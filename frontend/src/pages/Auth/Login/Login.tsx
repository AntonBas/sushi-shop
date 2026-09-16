import { useEffect, useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { useAuth } from '../../../context/useAuth'
import { API_BASE_URL } from '../../../config/env'
import { getErrorMessage } from '../../../api/errorMessage'
import Button from '../../../components/UI/Button/Button'
import Input from '../../../components/UI/Input/Input'
import { useResendVerification } from '../../../hooks/common/useResendVerification'
import styles from './Login.module.css'

const OAUTH2_ERROR_MESSAGES: Record<string, string> = {
  access_denied: 'Google login was cancelled.',
  unverified_email: 'Your Google account email is not verified. Please verify it with Google first.',
  oauth2_failed: 'Google login failed. Please try again.',
}

const UNVERIFIED_EMAIL_ERROR = 'Please verify your email before login'
const OAUTH_NO_PASSWORD_ERROR = 'This account uses Google sign-in and has no password set. Sign in with Google, or use "Forgot password?" to set one.'

export default function Login() {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [searchParams, setSearchParams] = useSearchParams()
  const [error, setError] = useState(() => {
    const oauthError = searchParams.get('error')
    return oauthError ? (OAUTH2_ERROR_MESSAGES[oauthError] || OAUTH2_ERROR_MESSAGES.oauth2_failed) : ''
  })
  const [loading, setLoading] = useState(false)
  const { login } = useAuth()
  const navigate = useNavigate()
  const { resend, cooldown, sending, message, messageType } = useResendVerification()

  useEffect(() => {
    if (searchParams.get('error')) {
      setSearchParams({}, { replace: true })
    }
  }, [searchParams, setSearchParams])

  const handleSubmit = async (e: React.SyntheticEvent) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await login({ email, password })
      navigate('/')
    } catch (err) {
      setError(getErrorMessage(err, 'Invalid email or password'))
    } finally {
      setLoading(false)
    }
  }

  const handleGoogleLogin = () => {
    window.location.href = `${API_BASE_URL}/oauth2/authorization/google`
  }

  return (
    <div className={styles.container}>
      <h1 className={styles.title}>Sign In</h1>

      {error && <div className={styles.error}>{error}</div>}

      {error === UNVERIFIED_EMAIL_ERROR && (
        <div className={styles.resendBlock}>
          {message && (
            <p className={messageType === 'error' ? styles.resendMessageError : styles.resendMessageSuccess}>{message}</p>
          )}
          <Button
            type="button"
            variant="secondary"
            onClick={() => resend(email)}
            loading={sending}
            disabled={cooldown > 0 || !email}
            style={{ width: '100%' }}
          >
            {cooldown > 0 ? `Resend in ${cooldown}s` : 'Resend verification email'}
          </Button>
        </div>
      )}

      {error === OAUTH_NO_PASSWORD_ERROR && (
        <div className={styles.resendBlock}>
          <Button type="button" variant="secondary" onClick={handleGoogleLogin} style={{ width: '100%' }}>
            Continue with Google
          </Button>
          <p className={styles.link}>
            Or <Link to="/forgot-password">set a password</Link> for this account.
          </p>
        </div>
      )}

      <form onSubmit={handleSubmit} className={styles.form}>
        <Input label="Email" type="email" value={email} onChange={setEmail} placeholder="your@email.com" />
        <Input label="Password" type="password" value={password} onChange={setPassword} placeholder="••••••••" />
        <div className={styles.forgot}>
          <Link to="/forgot-password">Forgot password?</Link>
        </div>
        <Button type="submit" loading={loading}>Login</Button>
      </form>

      <div className={styles.divider}>or</div>

      <Button variant="secondary" onClick={handleGoogleLogin} className={styles.googleBtn}>
        <svg width="18" height="18" viewBox="0 0 24 24">
          <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92a5.06 5.06 0 0 1-2.2 3.32v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.1z"/>
          <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
          <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
          <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
        </svg>
        Continue with Google
      </Button>

      <p className={styles.link}>
        Don't have an account? <Link to="/register">Register</Link>
      </p>
    </div>
  )
}