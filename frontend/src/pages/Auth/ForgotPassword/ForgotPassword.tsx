import { useState } from 'react'
import { Link } from 'react-router-dom'
import { AxiosError } from 'axios'
import { Mail } from 'lucide-react'
import * as authApi from '../../../api/auth'
import Button from '../../../components/UI/Button/Button'
import Input from '../../../components/UI/Input/Input'
import Modal from '../../../components/UI/Modal/Modal'
import styles from './ForgotPassword.module.css'

export default function ForgotPassword() {
  const [email, setEmail] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const [showSuccess, setShowSuccess] = useState(false)

  const handleSubmit = async (e: React.SyntheticEvent) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await authApi.forgotPassword({ email })
      setShowSuccess(true)
    } catch (err) {
      const error = err as AxiosError<{ message: string }>
      setError(error.response?.data?.message || 'Something went wrong')
    } finally {
      setLoading(false)
    }
  }

  return (
    <>
      <div className={styles.container}>
        <h1 className={styles.title}>Reset Password</h1>

        {error && <div className={styles.error}>{error}</div>}

        <form onSubmit={handleSubmit} className={styles.form}>
          <p className={styles.instruction}>
            Enter your email and we'll send you instructions to reset your password.
          </p>
          <Input label="Email" type="email" value={email} onChange={setEmail} placeholder="your@email.com" />
          <Button type="submit" loading={loading} style={{ width: '100%' }}>
            {loading ? 'Sending...' : 'Send Reset Instructions'}
          </Button>
        </form>

        <p className={styles.link}>
          <Link to="/login">Back to Login</Link>
        </p>
      </div>

      <Modal isOpen={showSuccess} onClose={() => setShowSuccess(false)}>
        <div className={styles.successModal}>
          <Mail size={48} className={styles.successIcon} />
          <h2>Check your email</h2>
          <p>We've sent password reset instructions to <strong>{email}</strong></p>
          <p className={styles.hint}>Check your spam folder if you don't see the email</p>
          <Button onClick={() => setShowSuccess(false)} style={{ width: '100%', marginTop: 16 }}>
            Got It
          </Button>
        </div>
      </Modal>
    </>
  )
}