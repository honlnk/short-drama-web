import { createContext, useContext, useEffect, useState, type ReactNode } from 'react'
import { getMe, type UserInfo } from '../api/admin'
import { TOKEN_KEY } from '../api/client'

interface AdminAuthContextValue {
  user: UserInfo | null
  token: string | null
  /** 初始化完成标记：挂载时校验 admin_token 期间为 false。 */
  ready: boolean
  login: (token: string, user: UserInfo) => void
  logout: () => void
}

const AdminAuthContext = createContext<AdminAuthContextValue | null>(null)

export function AdminAuthProvider({ children }: { children: ReactNode }) {
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
    <AdminAuthContext.Provider value={{ user, token, ready, login, logout }}>
      {children}
    </AdminAuthContext.Provider>
  )
}

export function useAdminAuth(): AdminAuthContextValue {
  const ctx = useContext(AdminAuthContext)
  if (!ctx) {
    throw new Error('useAdminAuth 必须在 AdminAuthProvider 内使用')
  }
  return ctx
}
