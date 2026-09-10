import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { AxiosError } from 'axios'
import { useAuth } from '../../../context/useAuth'
import Button from '../../../components/UI/Button/Button'
import Input from '../../../components/UI/Input/Input'
import Modal from '../../../components/UI/Modal/Modal'
import { ChevronDown, ChevronUp, Mail } from 'lucide-react'
import styles from './Register.module.css'

export default function Register() {
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [phone, setPhone] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [city, setCity] = useState('')
  const [street, setStreet] = useState('')
  const [house, setHouse] = useState('')
  const [apartment, setApartment] = useState('')
  const [showAddress, setShowAddress] = useState(false)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const [showSuccess, setShowSuccess] = useState(false)
  const { register } = useAuth()
  const navigate = useNavigate()

  const handleSubmit = async (e: React.SyntheticEvent) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await register({
        name, email, phone, password, confirmPassword,
        address: city ? { city, street, house, apartment: apartment || undefined } : undefined
      })
      setShowSuccess(true)
    } catch (err) {
      const error = err as AxiosError<{ message: string }>
      setError(error.response?.data?.message || 'Registration failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <>
      <div className={styles.container}>
        <h1 className={styles.title}>Create Account</h1>

        {error && <div className={styles.error}>{error}</div>}

        <form onSubmit={handleSubmit} className={styles.form}>
          <Input label="Name" value={name} onChange={setName} placeholder="Your name" />
          <Input label="Email" type="email" value={email} onChange={setEmail} placeholder="your@email.com" />
          <Input label="Phone" type="tel" value={phone} onChange={setPhone} placeholder="+380991234567" />
          <Input label="Password" type="password" value={password} onChange={setPassword} placeholder="Min 8 characters" />
          <Input label="Confirm Password" type="password" value={confirmPassword} onChange={setConfirmPassword} placeholder="Repeat password" />

          <button type="button" className={styles.toggle} onClick={() => setShowAddress(!showAddress)}>
            Delivery Address (optional)
            {showAddress ? <ChevronUp size={16} /> : <ChevronDown size={16} />}
          </button>

          {showAddress && (
            <div className={styles.addressGrid}>
              <Input label="City" value={city} onChange={setCity} placeholder="City" />
              <Input label="Street" value={street} onChange={setStreet} placeholder="Street" />
              <Input label="House" value={house} onChange={setHouse} placeholder="House" />
              <Input label="Apartment" value={apartment} onChange={setApartment} placeholder="Apt" />
            </div>
          )}

          <Button type="submit" loading={loading} style={{ width: '100%' }}>
            {loading ? 'Creating account...' : 'Create Account'}
          </Button>
        </form>

        <p className={styles.link}>
          Already have an account? <Link to="/login">Login</Link>
        </p>
      </div>

      <Modal isOpen={showSuccess} onClose={() => { setShowSuccess(false); navigate('/login') }}>
        <div className={styles.successModal}>
          <Mail size={48} className={styles.successIcon} />
          <h2>Check your email</h2>
          <p>We've sent a verification link to <strong>{email}</strong></p>
          <p className={styles.hint}>Click the link in the email to activate your account</p>
          <Button onClick={() => { setShowSuccess(false); navigate('/login') }} style={{ width: '100%', marginTop: 16 }}>
            Go to Login
          </Button>
        </div>
      </Modal>
    </>
  )
}