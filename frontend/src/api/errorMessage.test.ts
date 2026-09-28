import { describe, it, expect } from 'vitest'
import { AxiosError, AxiosHeaders } from 'axios'
import { getErrorMessage } from './errorMessage'

function axiosErrorWithData(data: unknown): AxiosError {
  return new AxiosError('Request failed', 'ERR_BAD_REQUEST', undefined, undefined, {
    status: 400,
    statusText: 'Bad Request',
    headers: {},
    config: { headers: new AxiosHeaders() },
    data,
  })
}

describe('getErrorMessage', () => {
  it('returns the fallback for a non-axios error', () => {
    expect(getErrorMessage(new Error('boom'), 'fallback')).toBe('fallback')
  })

  it('returns the fallback when the axios error has no response data', () => {
    expect(getErrorMessage(new AxiosError('Network Error'), 'fallback')).toBe('fallback')
  })

  it('returns the top-level message when there are no subErrors', () => {
    const err = axiosErrorWithData({ message: 'Email is already in use' })
    expect(getErrorMessage(err, 'fallback')).toBe('Email is already in use')
  })

  it('joins subError messages instead of the generic top-level message', () => {
    const err = axiosErrorWithData({
      message: 'Validation error',
      subErrors: [
        { object: 'registerRequest', field: 'phone', message: 'Phone must be 10-15 digits' },
        { object: 'registerRequest', field: 'email', message: 'Email is required' },
      ],
    })
    expect(getErrorMessage(err, 'fallback')).toBe('Phone must be 10-15 digits; Email is required')
  })

  it('falls back when response data has neither message nor subErrors', () => {
    const err = axiosErrorWithData({})
    expect(getErrorMessage(err, 'fallback')).toBe('fallback')
  })
})
