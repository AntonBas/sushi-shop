import { useCallback, useEffect, useState } from 'react'
import { isAxiosError } from 'axios'
import * as authApi from '../../api/auth'
import { getErrorMessage } from '../../api/errorMessage'

const FALLBACK_COOLDOWN_SECONDS = 60

export function useResendVerification(initialCooldown = 0) {
  const [cooldown, setCooldown] = useState(initialCooldown)
  const [sending, setSending] = useState(false)
  const [message, setMessage] = useState('')
  const [messageType, setMessageType] = useState<'success' | 'error'>('success')

  useEffect(() => {
    const id = setInterval(() => {
      setCooldown((s) => (s > 0 ? s - 1 : 0))
    }, 1000)
    return () => clearInterval(id)
  }, [])

  const resend = useCallback(async (email: string) => {
    if (cooldown > 0 || sending || !email) return
    setSending(true)
    setMessage('')
    try {
      const res = await authApi.resendVerification(email)
      setMessageType('success')
      setMessage('Verification email sent. Check your inbox.')
      setCooldown(res.cooldownSeconds)
    } catch (err) {
      setMessageType('error')
      setMessage(getErrorMessage(err, 'Failed to resend verification email'))
      if (isAxiosError(err) && err.response?.status === 429) {
        const retryAfter = Number(err.response.headers?.['retry-after'])
        setCooldown(Number.isFinite(retryAfter) && retryAfter > 0 ? retryAfter : FALLBACK_COOLDOWN_SECONDS)
      }
    } finally {
      setSending(false)
    }
  }, [cooldown, sending])

  const syncStatus = useCallback(async (email: string) => {
    if (!email) return
    try {
      const res = await authApi.getResendVerificationStatus(email)
      setCooldown(res.cooldownSeconds)
    } catch {
      /* best-effort sync; keep the current guess on failure */
    }
  }, [])

  return { resend, syncStatus, cooldown, sending, message, messageType }
}
