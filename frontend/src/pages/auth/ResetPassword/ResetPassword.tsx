import { useState } from 'react'
import { useSearchParams, useNavigate } from 'react-router-dom'
import { CheckCircle2 } from 'lucide-react'
import * as authApi from '../../../api/auth'
import Button from '../../../components/ui/Button/Button'
import Input from '../../../components/ui/Input/Input'
import Modal from '../../../components/ui/Modal/Modal'
import styles from './ResetPassword.module.css'

export default function ResetPassword() {
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const token = searchParams.get('token')

  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const [showSuccess, setShowSuccess] = useState(false)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await authApi.resetPassword({ token: token!, newPassword, confirmPassword })
      setShowSuccess(true)
    } catch (err: any) {
      setError(err.response?.data?.message || 'Something went wrong')
    } finally {
      setLoading(false)
    }
  }

  return (
    <>
      <div className={styles.container}>
        <h1 className={styles.title}>Create New Password</h1>

        {error && <div className={styles.error}>{error}</div>}

        <form onSubmit={handleSubmit} className={styles.form}>
          <p className={styles.instruction}>Enter your new password below.</p>
          <Input label="New Password" type="password" value={newPassword} onChange={setNewPassword} placeholder="Min 8 characters" />
          <Input label="Confirm Password" type="password" value={confirmPassword} onChange={setConfirmPassword} placeholder="Repeat password" />
          <Button type="submit" loading={loading} style={{ width: '100%' }}>
            {loading ? 'Resetting...' : 'Reset Password'}
          </Button>
        </form>
      </div>

      <Modal isOpen={showSuccess} onClose={() => { setShowSuccess(false); navigate('/login') }}>
        <div className={styles.successModal}>
          <CheckCircle2 size={48} className={styles.successIcon} />
          <h2>Password Reset!</h2>
          <p>Your password has been successfully changed.</p>
          <Button onClick={() => { setShowSuccess(false); navigate('/login') }} style={{ width: '100%', marginTop: 16 }}>
            Go to Login
          </Button>
        </div>
      </Modal>
    </>
  )
}