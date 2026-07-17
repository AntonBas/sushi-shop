import { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../../../context/AuthContext'
import Loading from '../../../components/ui/Loading/Loading'

export default function OAuth2Redirect() {
  const navigate = useNavigate()
  const { refreshUser } = useAuth()

  useEffect(() => {
    const params = new URLSearchParams(window.location.search)
    const token = params.get('token')
    const email = params.get('email')

    if (token && email) {
      localStorage.setItem('token', token)
      refreshUser().then(() => navigate('/')).catch(() => navigate('/login?error=oauth2_failed'))
    } else {
      navigate('/login?error=oauth2_failed')
    }
  }, [navigate, refreshUser])

  return <Loading text="Completing login..." />
}