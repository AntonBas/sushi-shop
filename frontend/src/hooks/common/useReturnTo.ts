import { useLocation } from 'react-router-dom'

interface ReturnToState {
  returnTo?: unknown
}

export function useReturnToState(): ReturnToState {
  const location = useLocation()
  return { returnTo: location.pathname + location.search }
}

export function useReturnTo(fallback: string): string {
  const location = useLocation()
  const returnTo = (location.state as ReturnToState | null)?.returnTo
  return typeof returnTo === 'string' && returnTo.startsWith(fallback) ? returnTo : fallback
}
