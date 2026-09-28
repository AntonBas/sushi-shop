import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { renderHook, act } from '@testing-library/react'
import type { Client, IFrame, StompConfig } from '@stomp/stompjs'
import { useOrderSocket } from './useOrderSocket'
import { issueWsTicket } from '../../api/auth'
import { useNotification } from '../../context/useNotification'

const { clients, FakeClient } = vi.hoisted(() => {
  const clients: InstanceType<typeof FakeClient>[] = []

  class FakeClient {
    config: StompConfig
    connectHeaders: Record<string, string> = {}
    activate = vi.fn()
    deactivate = vi.fn(() => Promise.resolve())

    constructor(config: StompConfig) {
      this.config = config
      clients.push(this)
    }
  }

  return { clients, FakeClient }
})

type FakeClient = InstanceType<typeof FakeClient>

vi.mock('@stomp/stompjs', () => ({ Client: FakeClient }))
vi.mock('sockjs-client', () => ({ default: vi.fn() }))
vi.mock('../../api/auth')
vi.mock('../../context/useNotification')

const showNotification = vi.fn()
const RECONNECT_WARNING = 'Live order updates unavailable, reconnecting...'

function lastClient() {
  return clients[clients.length - 1]
}

async function runBeforeConnect(client: FakeClient) {
  await act(async () => {
    await client.config.beforeConnect?.(client as unknown as Client)
  })
}

describe('useOrderSocket', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    clients.length = 0
    vi.mocked(useNotification).mockReturnValue({ showNotification } as unknown as ReturnType<typeof useNotification>)
    vi.mocked(issueWsTicket).mockResolvedValue('ticket-1')
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  it('activates the client on mount and deactivates it on unmount', () => {
    const { result, unmount } = renderHook(() => useOrderSocket(vi.fn()))
    const client = lastClient()

    expect(client.activate).toHaveBeenCalledTimes(1)
    expect(result.current.current).toBe(client)

    unmount()

    expect(client.deactivate).toHaveBeenCalled()
    expect(result.current.current).toBeNull()
  })

  it('authenticates with a fresh one-time ticket before each connect', async () => {
    renderHook(() => useOrderSocket(vi.fn()))
    const client = lastClient()

    await runBeforeConnect(client)

    expect(issueWsTicket).toHaveBeenCalledTimes(1)
    expect(client.connectHeaders).toEqual({ Authorization: 'Bearer ticket-1' })
  })

  it('passes the connected client to the latest onConnect callback', () => {
    const first = vi.fn()
    const second = vi.fn()
    const { rerender } = renderHook(({ cb }) => useOrderSocket(cb), { initialProps: { cb: first } })
    rerender({ cb: second })
    const client = lastClient()

    act(() => client.config.onConnect?.({} as IFrame))

    expect(first).not.toHaveBeenCalled()
    expect(second).toHaveBeenCalledWith(client)
    expect(clients).toHaveLength(1)
  })

  it('warns once per outage and again after a successful reconnect', () => {
    renderHook(() => useOrderSocket(vi.fn()))
    const client = lastClient()

    act(() => {
      client.config.onStompError?.({ headers: {} } as IFrame)
      client.config.onWebSocketError?.(new Event('error'))
    })
    expect(showNotification).toHaveBeenCalledTimes(1)
    expect(showNotification).toHaveBeenCalledWith(RECONNECT_WARNING, 'warning')

    act(() => {
      client.config.onConnect?.({} as IFrame)
      client.config.onWebSocketError?.(new Event('error'))
    })
    expect(showNotification).toHaveBeenCalledTimes(2)
  })

  it('restarts the client when the connection hangs past the watchdog timeout', async () => {
    vi.useFakeTimers()
    renderHook(() => useOrderSocket(vi.fn()))
    const client = lastClient()
    await runBeforeConnect(client)

    await act(async () => {
      await vi.advanceTimersByTimeAsync(15000)
    })

    expect(showNotification).toHaveBeenCalledWith(RECONNECT_WARNING, 'warning')
    expect(client.deactivate).toHaveBeenCalledTimes(1)
    expect(client.activate).toHaveBeenCalledTimes(2)
  })

  it('does not fire the watchdog once connected', async () => {
    vi.useFakeTimers()
    renderHook(() => useOrderSocket(vi.fn()))
    const client = lastClient()
    await runBeforeConnect(client)

    act(() => client.config.onConnect?.({} as IFrame))
    await act(async () => {
      await vi.advanceTimersByTimeAsync(15000)
    })

    expect(showNotification).not.toHaveBeenCalled()
    expect(client.deactivate).not.toHaveBeenCalled()
  })

  it('retries after the ticket request fails instead of stalling', async () => {
    vi.useFakeTimers()
    vi.mocked(issueWsTicket).mockRejectedValueOnce(new Error('network'))
    renderHook(() => useOrderSocket(vi.fn()))
    const client = lastClient()

    await runBeforeConnect(client)

    expect(showNotification).toHaveBeenCalledWith(RECONNECT_WARNING, 'warning')
    expect(client.deactivate).toHaveBeenCalledTimes(1)
    expect(client.activate).toHaveBeenCalledTimes(1)

    await act(async () => {
      await vi.advanceTimersByTimeAsync(5000)
    })

    expect(client.activate).toHaveBeenCalledTimes(2)
  })

  it('cancels a pending ticket retry on unmount', async () => {
    vi.useFakeTimers()
    vi.mocked(issueWsTicket).mockRejectedValueOnce(new Error('network'))
    const { unmount } = renderHook(() => useOrderSocket(vi.fn()))
    const client = lastClient()
    await runBeforeConnect(client)

    unmount()
    await act(async () => {
      await vi.advanceTimersByTimeAsync(5000)
    })

    expect(client.activate).toHaveBeenCalledTimes(1)
  })

  it('does not revive the client when unmounted while the ticket request is in flight', async () => {
    vi.useFakeTimers()
    let resolveTicket: (ticket: string) => void = () => {}
    vi.mocked(issueWsTicket).mockReturnValueOnce(new Promise((resolve) => { resolveTicket = resolve }))
    const { unmount } = renderHook(() => useOrderSocket(vi.fn()))
    const client = lastClient()

    const beforeConnect = client.config.beforeConnect?.(client as unknown as Client)
    unmount()
    await act(async () => {
      resolveTicket('late-ticket')
      await beforeConnect
      await vi.advanceTimersByTimeAsync(20000)
    })

    expect(client.connectHeaders).toEqual({})
    expect(client.activate).toHaveBeenCalledTimes(1)
    expect(showNotification).not.toHaveBeenCalled()
  })

  it('does not schedule a retry when unmounted while a failing ticket request is in flight', async () => {
    vi.useFakeTimers()
    let rejectTicket: (error: Error) => void = () => {}
    vi.mocked(issueWsTicket).mockReturnValueOnce(new Promise((_, reject) => { rejectTicket = reject }))
    const { unmount } = renderHook(() => useOrderSocket(vi.fn()))
    const client = lastClient()

    const beforeConnect = client.config.beforeConnect?.(client as unknown as Client)
    unmount()
    await act(async () => {
      rejectTicket(new Error('network'))
      await beforeConnect
      await vi.advanceTimersByTimeAsync(20000)
    })

    expect(client.activate).toHaveBeenCalledTimes(1)
    expect(showNotification).not.toHaveBeenCalled()
  })

  it('does not reactivate the client when unmounted while the watchdog restart is in progress', async () => {
    vi.useFakeTimers()
    const { unmount } = renderHook(() => useOrderSocket(vi.fn()))
    const client = lastClient()
    let finishDeactivate: () => void = () => {}
    client.deactivate.mockReturnValueOnce(new Promise((resolve) => { finishDeactivate = resolve }))
    await runBeforeConnect(client)

    await act(async () => {
      await vi.advanceTimersByTimeAsync(15000)
    })
    unmount()
    await act(async () => {
      finishDeactivate()
      await vi.advanceTimersByTimeAsync(0)
    })

    expect(client.activate).toHaveBeenCalledTimes(1)
  })
})
