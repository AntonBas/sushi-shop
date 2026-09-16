import { useEffect, useState } from 'react'
import { useSearchParams, useNavigate } from 'react-router-dom'
import { CheckCircle2, XCircle } from 'lucide-react'
import Button from '../../../components/UI/Button/Button'
import Loading from '../../../components/UI/Loading/Loading'
import * as usersApi from '../../../api/user'
import { getErrorMessage } from '../../../api/errorMessage'
import styles from '../EmailVerification/EmailVerification.module.css'

export default function VerifyEmailChange() {
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const token = searchParams.get('token')

  const [status, setStatus] = useState<'loading' | 'success' | 'error'>(token ? 'loading' : 'error')
  const [newEmail, setNewEmail] = useState('')
  const [error, setError] = useState('')

  useEffect(() => {
    if (!token) return
    let cancelled = false
    usersApi.confirmEmailChange(token)
      .then((user) => {
        if (cancelled) return
        setNewEmail(user.email)
        setStatus('success')
      })
      .catch((err) => {
        if (cancelled) return
        setError(getErrorMessage(err, 'The link is invalid or expired.'))
        setStatus('error')
      })
    return () => { cancelled = true }
  }, [token])

  if (status === 'loading') {
    return <Loading text="Confirming your new email..." />
  }

  return (
    <div className={styles.container}>
      <div className={`${styles.card} ${status === 'success' ? styles.success : styles.error}`}>
        {status === 'success' ? (
          <>
            <CheckCircle2 size={48} className={styles.icon} />
            <h2>Email Changed!</h2>
            <p>Your account email is now <strong>{newEmail}</strong>. Please log in again.</p>
            <Button onClick={() => navigate('/login')} style={{ width: '100%' }}>Go to Login</Button>
          </>
        ) : (
          <>
            <XCircle size={48} className={styles.icon} />
            <h2>Confirmation Failed</h2>
            <p>{error || 'The link is invalid or expired.'}</p>
            <Button onClick={() => navigate('/profile')} style={{ width: '100%' }}>Back to Profile</Button>
          </>
        )}
      </div>
    </div>
  )
}
