import { describe, it, expect, beforeEach } from 'vitest'
import { getAuthToken, setAuthToken, clearAuthToken } from './authToken'

describe('authToken', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  it('returns null when no token is stored', () => {
    expect(getAuthToken()).toBeNull()
  })

  it('stores and retrieves a token', () => {
    setAuthToken('abc123')
    expect(getAuthToken()).toBe('abc123')
  })

  it('overwrites a previously stored token', () => {
    setAuthToken('first')
    setAuthToken('second')
    expect(getAuthToken()).toBe('second')
  })

  it('removes the token', () => {
    setAuthToken('abc123')
    clearAuthToken()
    expect(getAuthToken()).toBeNull()
  })
})
