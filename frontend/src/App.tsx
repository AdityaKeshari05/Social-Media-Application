import { Route, Routes, Navigate } from 'react-router-dom'
import './App.css'
import { Layout } from './components/Layout'
import { FeedPage } from './pages/FeedPage'
import { PostPage } from './pages/PostPage'
import { LoginPage } from './pages/LoginPage'
import { RegisterPage } from './pages/RegisterPage'
import { ForgotPasswordPage } from './pages/ForgotPasswordPage'
import { ResetPasswordPage } from './pages/ResetPasswordPage'
import { ProfilePage } from './pages/ProfilePage'
import { SearchPage } from './pages/SearchPage'
import { GoogleOAuthPage } from './pages/GoogleOAuthPage'
import { CreatePostPage } from './pages/CreatePostPage'
import { EditPostPage } from './pages/EditPostPage'
import { SettingsPage } from './pages/SettingsPage'
import { PersonalDetailsPage } from './pages/PersonalDetailsPage'
import { EditProfilePage } from './pages/EditProfilePage'
import { MessagesPage } from './pages/MessagesPage'
import { useAuth } from './AuthContext'
import { lazy, Suspense, type ReactNode } from 'react'

const WebSocketTestPage = lazy(() =>
  import('./pages/WebSocketTestPage').then((m) => ({ default: m.WebSocketTestPage })),
)

function ProtectedRoute({ children }: { children: ReactNode }) {
  const { isAuthenticated } = useAuth()
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />
  }
  return children
}

function App() {
  const { isAuthenticated } = useAuth()
  return (
    <Layout>
      <Routes>
        <Route path="/" element={isAuthenticated ? <FeedPage /> : <Navigate to="/login" replace />} />
        <Route path="/posts/:id" element={<PostPage />} />
        <Route
          path="/posts/:id/edit"
          element={
            <ProtectedRoute>
              <EditPostPage />
            </ProtectedRoute>
          }
        />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/oauth2/google" element={<GoogleOAuthPage />} />
        <Route path="/forgot-password" element={<ForgotPasswordPage />} />
        <Route path="/reset-password" element={<ResetPasswordPage />} />
        <Route
          path="/me"
          element={
            <ProtectedRoute>
              <ProfilePage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/search"
          element={
            <ProtectedRoute>
              <SearchPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/posts/new"
          element={
            <ProtectedRoute>
              <CreatePostPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/settings"
          element={
            <ProtectedRoute>
              <SettingsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/settings/personal-details"
          element={
            <ProtectedRoute>
              <PersonalDetailsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/profile/edit"
          element={
            <ProtectedRoute>
              <EditProfilePage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/ws-test"
          element={
            <ProtectedRoute>
              <Suspense fallback={<div style={{ padding: 16 }}>Loading…</div>}>
                <WebSocketTestPage />
              </Suspense>
            </ProtectedRoute>
          }
        />
        <Route
          path="/messages"
          element={
            <ProtectedRoute>
              <MessagesPage />
            </ProtectedRoute>
          }
        />
        <Route path="/users/:id" element={<ProfilePage />} />
        <Route path="*" element={<Navigate to={isAuthenticated ? '/' : '/login'} replace />} />
      </Routes>
    </Layout>
  )
}

export default App
