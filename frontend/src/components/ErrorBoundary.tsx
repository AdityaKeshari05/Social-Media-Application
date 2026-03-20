import type { ReactNode } from 'react'
import { Component } from 'react'

type Props = { children: ReactNode }
type State = { error: unknown }

export class ErrorBoundary extends Component<Props, State> {
  state: State = { error: null }

  static getDerivedStateFromError(error: unknown) {
    return { error }
  }

  componentDidCatch(error: unknown) {
    console.error('App crashed:', error)
  }

  render() {
    if (this.state.error) {
      const message =
        this.state.error instanceof Error ? this.state.error.message : String(this.state.error)
      return (
        <div style={{ padding: 16, fontFamily: 'system-ui, sans-serif' }}>
          <h2 style={{ marginTop: 0 }}>Frontend crashed</h2>
          <p style={{ opacity: 0.85 }}>
            Check the console for stacktrace. Error message:
          </p>
          <pre style={{ whiteSpace: 'pre-wrap', background: '#111', color: '#fff', padding: 12, borderRadius: 8 }}>
            {message}
          </pre>
        </div>
      )
    }
    return this.props.children
  }
}

