import { useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { register as registerApi } from '../api/auth'
import { getApiErrorMessage } from '../api/client'
import { useAuth } from '../store/AuthContext'

export default function RegisterPage() {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [confirm, setConfirm] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const { login } = useAuth()
  const navigate = useNavigate()

  const submit = async (event: FormEvent) => {
    event.preventDefault()
    const name = username.trim()
    if (name.length < 2 || name.length > 50) {
      setError('用户名长度须在 2~50 之间')
      return
    }
    if (!password) {
      setError('请输入密码')
      return
    }
    if (password !== confirm) {
      setError('两次输入的密码不一致')
      return
    }
    setLoading(true)
    setError('')
    try {
      // 注册即登录：成功直接进入登录态并回首页
      const { token, user } = await registerApi(name, password)
      login(token, user)
      navigate('/', { replace: true })
    } catch (err) {
      setError(getApiErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  return (
    <main className="mx-auto w-full max-w-sm px-4 py-10">
      <h1 className="text-2xl font-bold">注册</h1>
      <p className="mt-1 text-sm text-neutral-400">注册即登录，马上开始追剧</p>

      <form onSubmit={submit} className="mt-6 space-y-4">
        <div>
          <label htmlFor="reg-username" className="mb-1 block text-sm text-neutral-300">
            用户名
          </label>
          <input
            id="reg-username"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            placeholder="2~50 个字符"
            autoComplete="username"
            className="w-full rounded-lg border border-neutral-700 bg-neutral-900 px-3 py-2 outline-none transition focus:border-rose-500"
          />
        </div>
        <div>
          <label htmlFor="reg-password" className="mb-1 block text-sm text-neutral-300">
            密码
          </label>
          <input
            id="reg-password"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="请输入密码"
            autoComplete="new-password"
            className="w-full rounded-lg border border-neutral-700 bg-neutral-900 px-3 py-2 outline-none transition focus:border-rose-500"
          />
        </div>
        <div>
          <label htmlFor="reg-confirm" className="mb-1 block text-sm text-neutral-300">
            确认密码
          </label>
          <input
            id="reg-confirm"
            type="password"
            value={confirm}
            onChange={(e) => setConfirm(e.target.value)}
            placeholder="再输入一次密码"
            autoComplete="new-password"
            className="w-full rounded-lg border border-neutral-700 bg-neutral-900 px-3 py-2 outline-none transition focus:border-rose-500"
          />
        </div>
        {error && <p className="text-sm text-red-400">{error}</p>}
        <button
          type="submit"
          disabled={loading}
          className="w-full rounded-lg bg-rose-500 py-2.5 font-medium text-white transition hover:bg-rose-600 disabled:opacity-50"
        >
          {loading ? '注册中…' : '注册'}
        </button>
      </form>

      <p className="mt-6 text-sm text-neutral-400">
        已有账号？
        <Link to="/login" className="ml-1 text-rose-400 transition hover:text-rose-300">
          去登录
        </Link>
      </p>
    </main>
  )
}
