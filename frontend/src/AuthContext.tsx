import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
} from 'react'
import { authApi } from './api'
import type { LoginStepOneResponse } from './api'
import type { ReactNode } from 'react'

interface AuthContextValue {
  token: string | null
  emailForOtp: string | null
  loginStepOne: (username: string, password: string) => Promise<LoginStepOneResponse | null>
  loginStepTwo: (otp: string) => Promise<boolean>
  loginWithToken: (token: string) => void
  logout: () => void
  isAuthenticated: boolean
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(() => localStorage.getItem('authToken'))
  const [emailForOtp, setEmailForOtp] = useState<string | null>(null)

  useEffect(() => {
    if (token) {
      localStorage.setItem('authToken', token)
    } else {
      localStorage.removeItem('authToken')
    }
  }, [token])

  const loginStepOne = useCallback(async (username: string, password: string) => {
    try {
      const { data } = await authApi.loginStepOne({ username, password })
      setEmailForOtp(data.email)
      return data
    } catch (error) {
      console.error(error)
      throw error
    }
  }, [])

  const loginStepTwo = useCallback(
    async (otp: string) => {
      if (!emailForOtp) return false
      try {
        const { data } = await authApi.loginStepTwo({ email: emailForOtp, otp })
        const tokenValue = data.Token
        setToken(tokenValue)
        setEmailForOtp(null)
        return true
      } catch (error) {
        console.error(error)
        throw error
      }
    },
    [emailForOtp],
  )

  const logout = useCallback(() => {
    setToken(null)
    setEmailForOtp(null)
  }, [])

  const loginWithToken = useCallback((tokenValue: string) => {
    setToken(tokenValue)
    setEmailForOtp(null)
  }, [])

  const value = useMemo<AuthContextValue>(
    () => ({
      token,
      emailForOtp,
      loginStepOne,
      loginStepTwo,
      loginWithToken,
      logout,
      isAuthenticated: !!token,
    }),
    [token, emailForOtp, loginStepOne, loginStepTwo, loginWithToken, logout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) {
    throw new Error('useAuth must be used within AuthProvider')
  }
  return ctx
}

