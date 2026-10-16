import { useEffect, useState } from 'react'

interface PingResult {
  ok: boolean
  message: string
}

export default function App() {
  const [ping, setPing] = useState<PingResult>({ ok: false, message: '检测中…' })

  useEffect(() => {
    fetch('/api/ping')
      .then((res) => res.json())
      .then(() => setPing({ ok: true, message: '后端连通' }))
      .catch(() => setPing({ ok: false, message: '后端未连通（请确认 8080 已启动）' }))
  }, [])

  return (
    <div className="flex min-h-screen flex-col items-center justify-center bg-neutral-950 text-white">
      <h1 className="text-2xl font-bold">短剧 APP · 用户端</h1>
      <p className="mt-2 text-sm text-neutral-400">阶段0 脚手架占位页 · Vite + React + Tailwind</p>
      <p className={`mt-4 text-sm ${ping.ok ? 'text-green-400' : 'text-red-400'}`}>
        /api/ping → {ping.message}
      </p>
    </div>
  )
}
