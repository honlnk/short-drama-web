import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../store/AuthContext'

/** 全局壳：顶栏（品牌 + 我的 + 登录态/登录入口）；手机档（<768px）底部 Tab 导航（PRD 5.3）。 */
export default function Layout() {
  const { user, ready, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/')
  }

  return (
    <div className="flex min-h-screen flex-col bg-neutral-950 pb-14 text-white md:pb-0">
      <header className="sticky top-0 z-20 border-b border-neutral-800 bg-neutral-950/90 backdrop-blur">
        <div className="mx-auto flex h-14 w-full max-w-6xl items-center px-4">
          <Link to="/" className="text-lg font-bold">
            短剧
          </Link>
          <span className="ml-3 rounded-full bg-rose-500/15 px-2 py-0.5 text-xs text-rose-400">推荐</span>
          <NavLink
            to="/mine"
            className="ml-4 hidden text-sm text-neutral-300 transition hover:text-white md:inline"
          >
            我的
          </NavLink>
          <div className="ml-auto flex items-center gap-3 text-sm">
            {!ready ? null : user ? (
              <>
                <span className="max-w-32 truncate text-neutral-300">{user.nickname || user.username}</span>
                <button
                  onClick={handleLogout}
                  className="rounded-lg border border-neutral-700 px-3 py-1 text-neutral-300 transition hover:border-neutral-500 hover:text-white"
                >
                  退出
                </button>
              </>
            ) : (
              <>
                <NavLink to="/login" className="text-neutral-300 transition hover:text-white">
                  登录
                </NavLink>
                <NavLink
                  to="/register"
                  className="rounded-lg bg-rose-500 px-3 py-1 font-medium text-white transition hover:bg-rose-600"
                >
                  注册
                </NavLink>
              </>
            )}
          </div>
        </div>
      </header>
      <Outlet />

      {/* 手机档底部 Tab（首页/我的）；平板与 PC 用顶栏导航 */}
      <nav
        aria-label="底部导航"
        className="fixed inset-x-0 bottom-0 z-20 flex h-14 border-t border-neutral-800 bg-neutral-950/95 backdrop-blur md:hidden"
      >
        <NavLink
          to="/"
          className={({ isActive }) =>
            `flex flex-1 flex-col items-center justify-center gap-0.5 text-xs ${
              isActive ? 'text-rose-400' : 'text-neutral-400'
            }`
          }
        >
          <span className="text-base leading-none">▶</span>
          首页
        </NavLink>
        <NavLink
          to="/mine"
          className={({ isActive }) =>
            `flex flex-1 flex-col items-center justify-center gap-0.5 text-xs ${
              isActive ? 'text-rose-400' : 'text-neutral-400'
            }`
          }
        >
          <span className="text-base leading-none">★</span>
          我的
        </NavLink>
      </nav>
    </div>
  )
}
