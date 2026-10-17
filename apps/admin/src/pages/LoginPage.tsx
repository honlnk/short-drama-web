import { useEffect } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { App, Button, Card, Form, Input } from 'antd'
import { login } from '../api/admin'
import { getApiErrorMessage } from '../api/client'
import { useAdminAuth } from '../store/AdminAuthContext'

/** 后台独立登录页（PRD 5.2：仅接受管理员角色令牌；与 C 端共用 /api/auth/login）。 */
export default function LoginPage() {
  const { login: adminLogin } = useAdminAuth()
  const { message } = App.useApp()
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const [form] = Form.useForm<{ username: string; password: string }>()

  useEffect(() => {
    form.setFieldsValue({ username: 'admin', password: 'admin123' })
  }, [form])

  const onFinish = async (values: { username: string; password: string }) => {
    try {
      const res = await login(values.username, values.password)
      if (res.user.role !== 'ADMIN') {
        message.error('该账号无后台权限')
        return
      }
      adminLogin(res.token, res.user)
      navigate(searchParams.get('from') || '/', { replace: true })
    } catch (err) {
      message.error(getApiErrorMessage(err))
    }
  }

  return (
    <div style={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', background: '#f5f5f5' }}>
      <Card title="短剧管理后台 · 登录" style={{ width: 360 }}>
        <Form form={form} layout="vertical" onFinish={onFinish}>
          <Form.Item name="username" label="用户名" rules={[{ required: true, message: '请输入用户名' }]}>
            <Input placeholder="请输入用户名" autoComplete="username" />
          </Form.Item>
          <Form.Item name="password" label="密码" rules={[{ required: true, message: '请输入密码' }]}>
            <Input.Password placeholder="请输入密码" autoComplete="current-password" />
          </Form.Item>
          <Button type="primary" htmlType="submit" block>
            登录
          </Button>
        </Form>
        <p style={{ marginTop: 12, color: '#999', fontSize: 12, textAlign: 'center' }}>
          演示账号：admin / admin123（仅管理员可登录后台）
        </p>
        <p style={{ color: '#999', fontSize: 12, textAlign: 'center' }}>
          <Link to="/">误入 C 端？点此返回</Link>
        </p>
      </Card>
    </div>
  )
}
