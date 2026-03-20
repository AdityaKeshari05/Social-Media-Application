import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import './index.css'
import App from './App.tsx'
import { AuthProvider } from './AuthContext'
import { ErrorBoundary } from './components/ErrorBoundary'
import { MessagingProvider } from './MessagingContext'

// Some browser bundles expect Node's `global`.
// Provide a safe alias to prevent runtime crashes (e.g. sockjs-client).
;(globalThis as any).global ??= globalThis

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <BrowserRouter>
      <ErrorBoundary>
        <AuthProvider>
          <MessagingProvider>
            <App />
          </MessagingProvider>
        </AuthProvider>
      </ErrorBoundary>
    </BrowserRouter>
  </StrictMode>,
)
