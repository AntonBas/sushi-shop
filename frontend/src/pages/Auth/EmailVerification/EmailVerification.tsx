import { useEffect, useState } from 'react'
import { useSearchParams, useNavigate, Link } from 'react-router-dom'
import { CheckCircle2, XCircle, Mail } from 'lucide-react'
import Button from '../../../components/UI/Button/Button'
import Input from '../../../components/UI/Input/Input'
import Loading from '../../../components/UI/Loading/Loading'
import { useResendVerification } from '../../../hooks/common/useResendVerification'
import * as authApi from '../../../api/auth'
import styles from './EmailVerification.module.css'

export default function EmailVerification() {
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const token = searchParams.get('token')
  const initialEmail = searchParams.get('email')
  const [tokenStatus, setTokenStatus] = useState<'loading' | 'success' | 'expired'>(token ? 'loading' : 'expired')
  const [email, setEmail] = useState(initialEmail ?? '')
  const { resend, syncStatus, cooldown, sending, message, messageType } = useResendVerification(token ? 0 : 60)

  useEffect(() => {
    if (!token) return
    let cancelled = false
    authApi.verifyEmail(token)
      .then(() => { if (!cancelled) setTokenStatus('success') })
      .catch(() => { if (!cancelled) setTokenStatus('expired') })
    return () => { cancelled = true }
  }, [token])

  useEffect(() => {
    if (!token && initialEmail) {
      syncStatus(initialEmail)
    }
  }, [token, initialEmail, syncStatus])

  if (tokenStatus === 'loading') {
    return <Loading text="Verifying your email..." />
  }

  if (tokenStatus === 'success') {
    return (
      <div className={styles.container}>
        <div className={`${styles.card} ${styles.success}`}>
          <CheckCircle2 size={48} className={styles.icon} />
          <h2>Email Verified!</h2>
          <p>Your account is now active</p>
          <Button onClick={() => navigate('/login')}>Go to Login</Button>
        </div>
      </div>
    )
  }

  return (
    <div className={styles.container}>
      <div className={`${styles.card} ${token ? styles.error : ''}`}>
        {token ? (
          <>
            <XCircle size={48} className={styles.icon} />
            <h2>Verification Failed</h2>
            <p>The link is invalid or expired. Enter your email to get a new one.</p>
          </>
        ) : (
          <>
            <Mail size={48} className={styles.icon} />
            <h2>Check your email</h2>
            <p>We've sent a verification link to <strong>{email || 'your email'}</strong></p>
          </>
        )}

        <Input label="Email" type="email" value={email} onChange={setEmail} placeholder="your@email.com" />

        {message && (
          <p className={messageType === 'error' ? styles.messageError : styles.messageSuccess}>{message}</p>
        )}

        <Button
          onClick={() => resend(email)}
          loading={sending}
          disabled={cooldown > 0 || !email}
          style={{ width: '100%' }}
        >
          {cooldown > 0 ? `Resend in ${cooldown}s` : 'Resend verification email'}
        </Button>

        <p className={styles.link}>
          <Link to="/register">Change email</Link> · <Link to="/login">I already verified</Link>
        </p>
      </div>
    </div>
  )
}
