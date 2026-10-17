import { Link } from 'react-router-dom'
import { useAuth } from '../store/AuthContext'

/** 「我的」页（US-04）：用户信息 + 我的收藏入口 + 退出登录。路由层由 RequireAuth 守卫。 */
export default function MinePage() {
  const { user, logout } = useAuth()

  if (!user) {
    return null
  }

  // 硬跳转而非 navigate('/')：本页被 RequireAuth 包裹，软导航会与「退出后 user=null 的守卫重定向」
  // 竞态，实测被带去 /login?from=/mine 而非首页（US-04 要求清 token 后跳首页）。
  const handleLogout = () => {
    logout()
    window.location.assign('/')
  }

  return (
    <main className="mx-auto w-full max-w-3xl px-4 py-6">
      <section className="flex items-center gap-4 rounded-xl bg-neutral-900 p-5">
        <div className="flex h-14 w-14 shrink-0 items-center justify-center rounded-full bg-rose-500/20 text-xl font-bold text-rose-300">
          {(user.nickname || user.username).slice(0, 1)}
        </div>
        <div className="min-w-0">
          <h1 className="truncate text-lg font-bold">{user.nickname || user.username}</h1>
          <p className="mt-1 text-sm text-neutral-400">
            @{user.username}
            <span className="ml-2 rounded-full bg-neutral-800 px-2 py-0.5 text-xs text-neutral-300">
              {user.role === 'ADMIN' ? '管理员' : '用户'}
            </span>
          </p>
        </div>
      </section>

      <nav className="mt-4 overflow-hidden rounded-xl bg-neutral-900">
        <Link
          to="/mine/favorites"
          className="flex items-center justify-between px-5 py-4 text-sm transition hover:bg-neutral-800"
        >
          <span>★ 我的收藏</span>
          <span className="text-neutral-500">›</span>
        </Link>
      </nav>

      <button
        onClick={handleLogout}
        className="mt-4 w-full rounded-xl border border-neutral-700 py-3 text-sm text-neutral-300 transition hover:border-rose-500/60 hover:text-rose-300"
      >
        退出登录
      </button>
    </main>
  )
}
