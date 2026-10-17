import type { ReactNode } from 'react'
import { Button, Layout, Space } from 'antd'
import { useAdminAuth } from '../store/AdminAuthContext'

/** 后台壳：顶栏（标题 + 当前管理员 + 退出）+ 内容区。仅 PC（PRD 5.2）。 */
export default function AdminLayout({ children }: { children: ReactNode }) {
  const { user, logout } = useAdminAuth()

  const handleLogout = () => {
    logout()
    window.location.assign('/login')
  }

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Layout.Header
        style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          background: '#001529',
        }}
      >
        <Space size="middle">
          <span style={{ color: '#fff', fontSize: 16, fontWeight: 600 }}>短剧管理后台</span>
        </Space>
        <Space>
          <span style={{ color: 'rgba(255,255,255,0.75)' }}>{user?.nickname || user?.username}</span>
          <Button size="small" ghost onClick={handleLogout}>
            退出
          </Button>
        </Space>
      </Layout.Header>
      <Layout.Content style={{ padding: 20 }}>
        <div style={{ maxWidth: 1100, margin: '0 auto' }}>{children}</div>
      </Layout.Content>
    </Layout>
  )
}
