import { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import Loading from '../../../components/ui/Loading/Loading'

export default function OAuth2Redirect() {
  const navigate = useNavigate()

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
  }, [navigate])

  return <Loading text="Completing login..." />
}