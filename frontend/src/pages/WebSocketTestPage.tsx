import { Client, type IMessage } from '@stomp/stompjs'
import { useEffect, useMemo, useRef, useState } from 'react'
import { useAuth } from '../AuthContext'

type WsMessage = {
  id: number
  conversationId: number
  senderId: number
  senderUsername: string
  content: string
  createdAt: string
  deliveredAt: string | null
  readAt: string | null
}

export function WebSocketTestPage() {
  const { token } = useAuth()

  const [status, setStatus] = useState<'disconnected' | 'connecting' | 'connected' | 'error'>('disconnected')
  const [error, setError] = useState<string | null>(null)
  const [messages, setMessages] = useState<WsMessage[]>([])

  const [recipientId, setRecipientId] = useState<string>('')
  const [content, setContent] = useState<string>('Hello from websocket')

  const clientRef = useRef<Client | null>(null)

  const canConnect = useMemo(() => !!token, [token])

  useEffect(() => {
    if (!canConnect) {
      setStatus('disconnected')
      setError('Login first (JWT token missing).')
      return
    }

    let active = true
    let client: Client | null = null

    ;(async () => {
      setError(null)
      setStatus('connecting')

      // Dynamically import SockJS so it never affects initial app boot.
      const mod = await import('sockjs-client')
      const SockJS = (mod as any).default as (url: string) => WebSocket

      if (!active) return

      client = new Client({
        // Authenticate at WebSocket handshake level for reliability:
        // /ws?token=JWT
        webSocketFactory: () => SockJS(`http://localhost:8080/ws?token=${encodeURIComponent(token!)}`),
        connectHeaders: {
          Authorization: `Bearer ${token}`,
        },
        debug: () => {
          // keep console clean for now
        },
        reconnectDelay: 3000,
        onConnect: () => {
          setStatus('connected')

          client?.subscribe('/user/queue/messages', (frame: IMessage) => {
            try {
              const parsed = JSON.parse(frame.body) as WsMessage
              setMessages((prev) => [parsed, ...prev])
            } catch (e) {
              console.error('Failed to parse message frame', e, frame.body)
            }
          })
        },
        onStompError: (frame) => {
          setStatus('error')
          setError(frame.headers['message'] || 'STOMP error')
        },
        onWebSocketError: () => {
          setStatus('error')
          setError('WebSocket error (check backend is running and /ws/** is permitted).')
        },
        onDisconnect: () => {
          setStatus('disconnected')
        },
      })

      client.activate()
      clientRef.current = client
    })().catch((e) => {
      console.error(e)
      setStatus('error')
      setError(e instanceof Error ? e.message : String(e))
    })

    return () => {
      active = false
      clientRef.current = null
      void client?.deactivate()
    }
  }, [canConnect, token])

  function send() {
    const client = clientRef.current
    if (!client || status !== 'connected') return

    const rid = Number(recipientId)
    if (!Number.isFinite(rid) || rid <= 0) {
      setError('Enter a valid recipientId (numeric user id).')
      return
    }
    if (!content.trim()) {
      setError('Content cannot be empty.')
      return
    }

    setError(null)
    client.publish({
      destination: '/app/chat.send',
      headers: {
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify({ recipientId: rid, content }),
    })
    setContent('')
  }

  return (
    <div style={{ maxWidth: 900, margin: '0 auto', padding: 16 }}>
      <h2>WebSocket Test</h2>

      <div style={{ marginBottom: 12 }}>
        <strong>Status:</strong> {status}
        {error ? (
          <div style={{ marginTop: 8, color: 'crimson' }}>
            <strong>Error:</strong> {error}
          </div>
        ) : null}
      </div>

      <div
        style={{
          display: 'grid',
          gridTemplateColumns: '160px 1fr 120px',
          gap: 8,
          alignItems: 'center',
          marginBottom: 16,
        }}
      >
        <input
          value={recipientId}
          onChange={(e) => setRecipientId(e.target.value)}
          placeholder="recipientId (e.g. 75)"
        />
        <input value={content} onChange={(e) => setContent(e.target.value)} placeholder="Message content" />
        <button onClick={send} disabled={status !== 'connected'}>
          Send
        </button>
      </div>

      <div style={{ border: '1px solid #333', borderRadius: 8, padding: 12 }}>
        <div style={{ marginBottom: 8 }}>
          <strong>Incoming frames (/user/queue/messages)</strong>
        </div>
        {messages.length === 0 ? (
          <div style={{ opacity: 0.8 }}>No messages received yet.</div>
        ) : (
          <ul style={{ listStyle: 'none', padding: 0, margin: 0, display: 'grid', gap: 8 }}>
            {messages.map((m) => (
              <li key={`${m.id}-${m.createdAt}`} style={{ border: '1px solid #444', borderRadius: 8, padding: 10 }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', gap: 8 }}>
                  <div>
                    <strong>{m.senderUsername}</strong> (id {m.senderId}) → conv {m.conversationId}
                  </div>
                  <div style={{ opacity: 0.8, fontSize: 12 }}>{m.createdAt}</div>
                </div>
                <div style={{ marginTop: 6 }}>{m.content}</div>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  )
}

