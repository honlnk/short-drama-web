import { useState, type FormEvent } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { login as loginApi } from '../api/auth'
import { getApiErrorMessage } from '../api/client'
import { useAuth } from '../store/AuthContext'

export default function LoginPage() {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const { login } = useAuth()
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const from = searchParams.get('from') || '/'

  const submit = async (event: FormEvent) => {
    event.preventDefault()
    if (!username.trim() || !password) {
      setError('请输入用户名和密码')
      return
    }
    setLoading(true)
    setError('')
    try {
      const { token, user } = await loginApi(username.trim(), password)
      login(token, user)
      navigate(from, { replace: true })
    } catch (err) {
      setError(getApiErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  return (
    <main className="mx-auto w-full max-w-sm px-4 py-10">
      <h1 className="text-2xl font-bold">登录</h1>
      <p className="mt-1 text-sm text-neutral-400">登录后可点赞、收藏短剧</p>

      <form onSubmit={submit} className="mt-6 space-y-4">
        <div>
          <label htmlFor="login-username" className="mb-1 block text-sm text-neutral-300">
            用户名
          </label>
          <input
            id="login-username"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            placeholder="请输入用户名"
            autoComplete="username"
            className="w-full rounded-lg border border-neutral-700 bg-neutral-900 px-3 py-2 outline-none transition focus:border-rose-500"
          />
        </div>
        <div>
          <label htmlFor="login-password" className="mb-1 block text-sm text-neutral-300">
            密码
          </label>
          <input
            id="login-password"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="请输入密码"
            autoComplete="current-password"
            className="w-full rounded-lg border border-neutral-700 bg-neutral-900 px-3 py-2 outline-none transition focus:border-rose-500"
          />
        </div>
        {error && <p className="text-sm text-red-400">{error}</p>}
        <button
          type="submit"
          disabled={loading}
          className="w-full rounded-lg bg-rose-500 py-2.5 font-medium text-white transition hover:bg-rose-600 disabled:opacity-50"
        >
          {loading ? '登录中…' : '登录'}
        </button>
      </form>

      <div className="mt-4 flex flex-wrap items-center gap-2 text-sm text-neutral-400">
        <span>演示账号快捷填充：</span>
        <button
          type="button"
          onClick={() => {
            setUsername('demo')
            setPassword('demo123')
          }}
          className="rounded-md border border-neutral-700 px-2 py-0.5 transition hover:text-white"
        >
          demo / demo123
        </button>
        <button
          type="button"
          onClick={() => {
            setUsername('admin')
            setPassword('admin123')
          }}
          className="rounded-md border border-neutral-700 px-2 py-0.5 transition hover:text-white"
        >
          admin / admin123
        </button>
      </div>

      <p className="mt-6 text-sm text-neutral-400">
        还没有账号？
        <Link to="/register" className="ml-1 text-rose-400 transition hover:text-rose-300">
          去注册
        </Link>
      </p>
    </main>
  )
}
