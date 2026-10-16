import { useEffect, useState } from 'react'
import { Button, Card, Space, Tag } from 'antd'

interface PingState {
  ok: boolean
  text: string
}

export default function App() {
  const [ping, setPing] = useState<PingState>({ ok: false, text: '检测中…' })

  useEffect(() => {
    fetch('/api/ping')
      .then((res) => res.json())
      .then(() => setPing({ ok: true, text: '后端连通' }))
      .catch(() => setPing({ ok: false, text: '后端未连通（请确认 8080 已启动）' }))
  }, [])

  return (
    <div className="flex min-h-screen items-center justify-center bg-gray-100">
      <Card title="短剧管理后台" className="w-96 text-center">
        <p className="text-gray-500">阶段0 脚手架占位页 · Vite + React + Ant Design</p>
        <Space className="mt-4">
          <Tag color={ping.ok ? 'green' : 'red'}>/api/ping → {ping.text}</Tag>
        </Space>
        <div className="mt-4">
          <Button type="primary" onClick={() => window.location.reload()}>
            重新检测
          </Button>
        </div>
      </Card>
    </div>
  )
}
