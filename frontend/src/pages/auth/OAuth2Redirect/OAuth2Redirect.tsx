import { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../../../context/AuthContext'
import styles from './OAuth2Redirect.module.css'

export default function OAuth2Redirect() {
  const navigate = useNavigate()
  const { login } = useAuth()

  useEffect(() => {
    const params = new URLSearchParams(window.location.search)
    const token = params.get('token')
    const email = params.get('email')

    if (token && email) {
      localStorage.setItem('token', token)
      navigate('/')
    } else {
      navigate('/login?error=oauth2_failed')
    }
  }, [navigate, login])

  return (
    <div className={styles.container}>
      <div className={styles.spinner} />
      <p>Completing login...</p>
    </div>
  )
}