import { useState, useCallback } from 'react'
import { AxiosError } from 'axios'

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

  const execute = useCallback(async (apiCall: () => Promise<T>) => {
    setState({ data: null, loading: true, error: null })
    try {
      const data = await apiCall()
      setState({ data, loading: false, error: null })
      return data
    } catch (err) {
      const error = err as AxiosError<{ message: string }>
      const message = error.response?.data?.message || error.message || 'Something went wrong'
      setState({ data: null, loading: false, error: message })
      throw error
    }
  }, [])

  return { ...state, execute }
}