import { describe, it, expect } from 'vitest'
import { toDateTimeLocalValue } from './dateTimeLocal'

describe('toDateTimeLocalValue', () => {
  it('round-trips through the datetime-local value without shifting the instant', () => {
    const serverValue = '2026-09-27T15:30:00Z'

    const localValue = toDateTimeLocalValue(serverValue)

    expect(new Date(localValue).toISOString()).toBe('2026-09-27T15:30:00.000Z')
  })
})
