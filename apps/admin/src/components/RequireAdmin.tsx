import { useEffect, useState, type ReactNode } from 'react'
import { Navigate } from 'react-router-dom'
import { useAdminAuth } from '../store/AdminAuthContext'
import AdminLayout from './AdminLayout'

/**
 * 后台路由守卫：无 token 跳 /login；user.role≠ADMIN（如误存了 C 端用户 token）清 token 拒绝进入。
 * 硬闸门在后端 /api/admin/** 的 ROLE_ADMIN（403），此处是体验层。
 */
export default function RequireAdmin({ children }: { children: ReactNode }) {
  const { user, ready, logout } = useAdminAuth()
  const [rejected, setRejected] = useState(false)

  useEffect(() => {
    if (ready && user && user.role !== 'ADMIN') {
      logout()
      setRejected(true)
    }
  }, [ready, user, logout])

  if (!ready) {
    return null
  }
  if (!user || rejected) {
    return <Navigate to="/login" replace />
  }
  return <AdminLayout>{children}</AdminLayout>
}
