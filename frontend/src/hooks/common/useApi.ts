import { useState, useCallback } from 'react'
import { useNotification } from '../../context/useNotification'
import { useDelayedLoading } from './useDelayedLoading'
import { getErrorMessage } from '../../api/errorMessage'

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
  const loading = useDelayedLoading(state.loading)

  const execute = useCallback(async (apiCall: () => Promise<T>, successMessage?: string) => {
    setState(prev => ({ ...prev, loading: true, error: null }))
    try {
      const data = await apiCall()
      setState({ data, loading: false, error: null })
      if (successMessage) showNotification(successMessage, 'success')
      return data
    } catch (err) {
      const message = getErrorMessage(err, 'Something went wrong')
      setState(prev => ({ ...prev, loading: false, error: message }))
      showNotification(message, 'error')
      throw err
    }
  }, [showNotification])

  return { ...state, loading, execute }
}