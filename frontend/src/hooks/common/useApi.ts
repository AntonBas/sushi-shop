import { useState, useCallback } from 'react'
import { AxiosError } from 'axios'
import { useNotification } from '../../context/NotificationContext'

interface ApiState<T> {
  data: T | null
  loading: boolean
  error: string | null
}

export function useApi<T>() {
  const [state, setState] = useState<ApiState<T>>({
    data: null,
    loading: false,
    error: null
  })
  const { showNotification } = useNotification()

  const execute = useCallback(async (apiCall: () => Promise<T>, successMessage?: string) => {
    setState({ data: null, loading: true, error: null })
    try {
      const data = await apiCall()
      setState({ data, loading: false, error: null })
      if (successMessage) showNotification(successMessage, 'success')
      return data
    } catch (err) {
      const error = err as AxiosError<{ message: string }>
      const message = error.response?.data?.message || error.message || 'Something went wrong'
      setState({ data: null, loading: false, error: message })
      showNotification(message, 'error')
      throw error
    }
  }, [showNotification])

  return { ...state, execute }
}