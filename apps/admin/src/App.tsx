import { App as AntApp, ConfigProvider } from 'antd'
import zhCN from 'antd/locale/zh_CN'
import { RouterProvider } from 'react-router-dom'
import { router } from './router'
import { AdminAuthProvider } from './store/AdminAuthContext'

/** 管理后台入口：antd 中文文案 + AuthProvider（admin_token 隔离）+ 路由（守卫见 router）。 */
export default function App() {
  return (
    <ConfigProvider locale={zhCN}>
      <AntApp>
        <AdminAuthProvider>
          <RouterProvider router={router} />
        </AdminAuthProvider>
      </AntApp>
    </ConfigProvider>
  )
}
