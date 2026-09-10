import { useState, useEffect } from 'react'
import { useSearchParams, useNavigate } from 'react-router-dom'
import { CheckCircle2, XCircle } from 'lucide-react'
import Button from '../../../components/UI/Button/Button'
import Loading from '../../../components/UI/Loading/Loading'
import * as authApi from '../../../api/auth'
import styles from './EmailVerification.module.css'

export default function EmailVerification() {
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const token = searchParams.get('token')
  const [status, setStatus] = useState<'loading' | 'success' | 'error'>(token ? 'loading' : 'error')

  useEffect(() => {
    if (!token) return
    let cancelled = false
    authApi.verifyEmail(token)
      .then(() => { if (!cancelled) setStatus('success') })
      .catch(() => { if (!cancelled) setStatus('error') })
    return () => { cancelled = true }
  }, [token])

  if (status === 'loading') {
    return <Loading text="Verifying your email..." />
  }

  if (status === 'error') {
    return (
      <div className={styles.container}>
        <div className={`${styles.card} ${styles.error}`}>
          <XCircle size={48} className={styles.icon} />
          <h2>Verification Failed</h2>
          <p>The link is invalid or expired</p>
          <Button onClick={() => navigate('/login')}>Go to Login</Button>
        </div>
      </div>
    )
  }

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