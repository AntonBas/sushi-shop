import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '@/context/useAuth'
import { getErrorMessage, getFieldErrors, getGlobalErrorMessage } from '@/api/errorMessage'
import Button from '@/components/UI/Button/Button'
import Input from '@/components/UI/Input/Input'
import { ChevronDown, ChevronUp } from 'lucide-react'
import { validateRegisterForm } from './registerValidation'
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
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})
  const [loading, setLoading] = useState(false)
  const { register } = useAuth()
  const navigate = useNavigate()

  const handleSubmit = async (e: React.SyntheticEvent) => {
    e.preventDefault()
    setError('')
    const clientErrors = validateRegisterForm({ name, email, phone, password, confirmPassword, city, street, house, apartment })
    setFieldErrors(clientErrors)
    if (Object.keys(clientErrors).length > 0) {
      if (clientErrors['address.city'] || clientErrors['address.street'] || clientErrors['address.house']) setShowAddress(true)
      return
    }
    setLoading(true)
    try {
      await register({
        name, email, phone, password, confirmPassword,
        address: city ? { city, street, house, apartment: apartment || undefined } : undefined
      })
      void navigate(`/verify-email?email=${encodeURIComponent(email)}`)
    } catch (err) {
      const serverFieldErrors = getFieldErrors(err)
      setFieldErrors(serverFieldErrors)
      setError(Object.keys(serverFieldErrors).length > 0
        ? getGlobalErrorMessage(err)
        : getErrorMessage(err, 'Registration failed'))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className={styles.container}>
      <h1 className={styles.title}>Create Account</h1>

      {error && <div className={styles.error}>{error}</div>}

      <form onSubmit={(e) => void handleSubmit(e)} className={styles.form}>
        <Input label="Name" name="name" autoComplete="name" value={name} onChange={setName} placeholder="Your name" error={fieldErrors.name} />
        <Input label="Email" name="email" autoComplete="email" type="email" value={email} onChange={setEmail} placeholder="your@email.com" error={fieldErrors.email} />
        <Input label="Phone" name="phone" autoComplete="tel" type="tel" value={phone} onChange={setPhone} placeholder="+380991234567" error={fieldErrors.phone} />
        <Input label="Password" name="password" autoComplete="new-password" type="password" value={password} onChange={setPassword} placeholder="Min 8 characters" error={fieldErrors.password} />
        <Input label="Confirm Password" name="confirmPassword" autoComplete="new-password" type="password" value={confirmPassword} onChange={setConfirmPassword} placeholder="Repeat password" error={fieldErrors.confirmPassword} />

        <button type="button" className={styles.toggle} onClick={() => setShowAddress(!showAddress)} aria-expanded={showAddress}>
          Delivery Address (optional)
          {showAddress ? <ChevronUp size={16} /> : <ChevronDown size={16} />}
        </button>

        {showAddress && (
          <div className={styles.addressGrid}>
            <Input label="City" name="city" autoComplete="address-level2" value={city} onChange={setCity} placeholder="City" error={fieldErrors['address.city']} />
            <Input label="Street" name="street" autoComplete="address-line1" value={street} onChange={setStreet} placeholder="Street" error={fieldErrors['address.street']} />
            <Input label="House" name="house" value={house} onChange={setHouse} placeholder="House" error={fieldErrors['address.house']} />
            <Input label="Apartment" name="apartment" value={apartment} onChange={setApartment} placeholder="Apt" error={fieldErrors['address.apartment']} />
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
  )
}
