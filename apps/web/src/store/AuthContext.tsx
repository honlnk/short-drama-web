import { createContext, useContext, useEffect, useState, type ReactNode } from 'react'
import { getMe, type UserInfo } from '../api/auth'
import { TOKEN_KEY } from '../api/client'

interface AuthContextValue {
  user: UserInfo | null
  token: string | null
  /** 初始化完成标记：挂载时校验 localStorage token 期间为 false，避免闪烁。 */
  ready: boolean
  login: (token: string, user: UserInfo) => void
  logout: () => void
}

const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserInfo | null>(null)
  const [token, setToken] = useState<string | null>(() => localStorage.getItem(TOKEN_KEY))
  const [ready, setReady] = useState(false)

  useEffect(() => {
    const stored = localStorage.getItem(TOKEN_KEY)
    if (!stored) {
      setReady(true)
      return
    }
    getMe()
      .then(setUser)
      .catch(() => {
        // token 过期/伪造：清掉，降级为访客，不打断当前页
        localStorage.removeItem(TOKEN_KEY)
        setToken(null)
      })
      .finally(() => setReady(true))
  }, [])

  const login = (newToken: string, newUser: UserInfo) => {
    localStorage.setItem(TOKEN_KEY, newToken)
    setToken(newToken)
    setUser(newUser)
  }

  const logout = () => {
    localStorage.removeItem(TOKEN_KEY)
    setToken(null)
    setUser(null)
  }

  return (
    <AuthContext.Provider value={{ user, token, ready, login, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext)
  if (!ctx) {
    throw new Error('useAuth 必须在 AuthProvider 内使用')
  }
  return ctx
}
