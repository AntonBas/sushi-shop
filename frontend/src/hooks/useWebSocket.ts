import { useEffect, useRef, useState } from 'react'
import { Client } from '@stomp/stompjs'
import type { OrderStatusUpdate } from '../types'

export function useOrderTracking(orderId: number | null) {
  const [status, setStatus] = useState<string | null>(null)
  const clientRef = useRef<Client | null>(null)

  useEffect(() => {
    if (!orderId) return

    const client = new Client({
      brokerURL: `ws://localhost:8080/ws`,
      onConnect: () => {
        client.subscribe(`/topic/orders/${orderId}`, (msg) => {
          const update: OrderStatusUpdate = JSON.parse(msg.body)
          setStatus(update.status)
        })
      }
    })

    client.activate()
    clientRef.current = client

    return () => { client.deactivate() }
  }, [orderId])

  return status
}