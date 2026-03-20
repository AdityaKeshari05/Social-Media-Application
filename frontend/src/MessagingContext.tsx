import { Client, type IMessage } from '@stomp/stompjs'
import { createContext, useContext, useEffect, useMemo, useRef, useState } from 'react'
import { useAuth } from './AuthContext'
import type { MessageDtoApi } from './api'

export type ReadReceiptEventApi = {
  conversationId: number
  readerId: number
  readAt: string
}

type MessagingContextValue = {
  connected: boolean
  lastMessage: MessageDtoApi | null
  lastReadReceipt: ReadReceiptEventApi | null
  sendMessageWs: (recipientId: number, content: string) => void
  sendReadWs: (conversationId: number) => void
}

const MessagingContext = createContext<MessagingContextValue | undefined>(undefined)

export function MessagingProvider({ children }: { children: React.ReactNode }) {
  const { token } = useAuth()
  const [connected, setConnected] = useState(false)
  const [lastMessage, setLastMessage] = useState<MessageDtoApi | null>(null)
  const [lastReadReceipt, setLastReadReceipt] = useState<ReadReceiptEventApi | null>(null)

  const clientRef = useRef<Client | null>(null)

  useEffect(() => {
    if (!token) {
      setConnected(false)
      clientRef.current?.deactivate()
      clientRef.current = null
      return
    }

    let active = true

    ;(async () => {
      const mod = await import('sockjs-client')
      const SockJS = (mod as any).default as (url: string) => WebSocket

      if (!active) return

      const client = new Client({
        webSocketFactory: () => SockJS(`http://localhost:8080/ws?token=${encodeURIComponent(token)}`),
        connectHeaders: {
          Authorization: `Bearer ${token}`,
        },
        reconnectDelay: 3000,
        debug: () => {},
        onConnect: () => {
          setConnected(true)

          client.subscribe('/user/queue/messages', (frame: IMessage) => {
            try {
              const parsed = JSON.parse(frame.body) as MessageDtoApi
              setLastMessage(parsed)
            } catch (e) {
              console.error('Failed to parse ws message', e, frame.body)
            }
          })

          client.subscribe('/user/queue/read-receipts', (frame: IMessage) => {
            try {
              const parsed = JSON.parse(frame.body) as ReadReceiptEventApi
              setLastReadReceipt(parsed)
            } catch (e) {
              console.error('Failed to parse read receipt', e, frame.body)
            }
          })
        },
        onDisconnect: () => setConnected(false),
        onWebSocketClose: () => setConnected(false),
        onWebSocketError: () => setConnected(false),
      })

      client.activate()
      clientRef.current = client
    })().catch((e) => {
      console.error(e)
      setConnected(false)
    })

    return () => {
      active = false
      setConnected(false)
      void clientRef.current?.deactivate()
      clientRef.current = null
    }
  }, [token])

  const value = useMemo<MessagingContextValue>(
    () => ({
      connected,
      lastMessage,
      lastReadReceipt,
      sendMessageWs: (recipientId, content) => {
        if (!token) return
        const client = clientRef.current
        if (!client || !client.connected) return
        client.publish({
          destination: '/app/chat.send',
          headers: { Authorization: `Bearer ${token}` },
          body: JSON.stringify({ recipientId, content }),
        })
      },
      sendReadWs: (conversationId) => {
        if (!token) return
        const client = clientRef.current
        if (!client || !client.connected) return
        client.publish({
          destination: '/app/chat.read',
          headers: { Authorization: `Bearer ${token}` },
          body: JSON.stringify({ conversationId }),
        })
      },
    }),
    [connected, lastMessage, lastReadReceipt, token],
  )

  return <MessagingContext.Provider value={value}>{children}</MessagingContext.Provider>
}

export function useMessaging() {
  const ctx = useContext(MessagingContext)
  if (!ctx) throw new Error('useMessaging must be used within MessagingProvider')
  return ctx
}

