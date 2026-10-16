import { Navigate, useLocation } from 'react-router-dom'
import type { ReactNode } from 'react'
import { useAuth } from '../store/AuthContext'

/**
 * 整页登录守卫（备好供里程碑 4「我的收藏」等页面使用）。
 * 本阶段点赞/收藏按钮采用「点击时判断」而非整页守卫，故暂无路由挂载。
 */
export default function RequireAuth({ children }: { children: ReactNode }) {
  const { user, ready } = useAuth()
  const location = useLocation()

  if (!ready) {
    return null
  }
  if (!user) {
    const from = encodeURIComponent(location.pathname + location.search)
    return <Navigate to={`/login?from=${from}`} replace />
  }
  return children
}
