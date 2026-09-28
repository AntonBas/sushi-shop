import { useState, useCallback } from 'react'
import { AxiosError } from 'axios'
import { useNotification } from '../../context/useNotification'
import { useDelayedLoading } from './useDelayedLoading'
import { getErrorMessage } from '../../api/errorMessage'

interface ApiState<T> {
  data: T | null
  loading: boolean
  error: string | null
  errorStatus: number | null
}

interface RequestOptions {
  silentStatuses?: number[]
}

export function useApi<T>() {
  const [state, setState] = useState<ApiState<T>>({
    data: null,
    loading: false,
    error: null,
    errorStatus: null
  })
  const { showNotification } = useNotification()
  const loading = useDelayedLoading(state.loading)

  const execute = useCallback(async (apiCall: () => Promise<T>, options: RequestOptions = {}) => {
    setState(prev => ({ ...prev, loading: true, error: null, errorStatus: null }))
    try {
      const data = await apiCall()
      setState({ data, loading: false, error: null, errorStatus: null })
      return data
    } catch (err) {
      const message = getErrorMessage(err, 'Something went wrong')
      const errorStatus = err instanceof AxiosError ? err.response?.status ?? null : null
      setState(prev => ({ ...prev, loading: false, error: message, errorStatus }))
      if (errorStatus === null || !options.silentStatuses?.includes(errorStatus)) {
        showNotification(message, 'error')
      }
      throw err
    }
  }, [showNotification])

  const run = useCallback(async (apiCall: () => Promise<T>, options?: RequestOptions): Promise<T | null> => {
    try {
      return await execute(apiCall, options)
    } catch {
      return null
    }
  }, [execute])

  return { ...state, loading, execute, run }
}