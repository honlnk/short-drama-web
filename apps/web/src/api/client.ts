import axios from 'axios'

export const TOKEN_KEY = 'token'

declare module 'axios' {
  export interface AxiosRequestConfig {
    /** true 时 401 不触发跳转登录页（如启动时登录态校验，失败应静默降级为访客）。 */
    skipAuthRedirect?: boolean
  }
}

/** 全站唯一 axios 实例：走 Vite 代理（/api、/uploads → 8080），自动附带 JWT。 */
const client = axios.create({ baseURL: '' })

client.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY)
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

client.interceptors.response.use(
  (response) => {
    // 后端统一包体 {code,message,data}（PRD 接口规范）：此处统一拆包，
    // 页面层 res.data 直接就是业务数据；code!=0 但 HTTP 200 的业务错误转为 reject。
    const body = response.data as { code?: number; message?: string; data?: unknown } | undefined
    if (body && typeof body === 'object' && typeof body.code === 'number') {
      if (body.code !== 0) {
        return Promise.reject(
          new axios.AxiosError(body.message ?? '请求失败', String(body.code), response.config, response.request, {
            ...response,
            data: body,
          }),
        )
      }
      response.data = body.data
    }
    return response
  },
  (error) => {
    if (axios.isAxiosError(error) && error.response?.status === 401 && !error.config?.skipAuthRedirect) {
      localStorage.removeItem(TOKEN_KEY)
      const { pathname, search } = window.location
      if (!pathname.startsWith('/login')) {
        window.location.assign(`/login?from=${encodeURIComponent(pathname + search)}`)
      }
    }
    return Promise.reject(error)
  },
)

/** 从后端 {code,message,data} 错误包体或网络错误中提取用户可读信息。 */
export function getApiErrorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    const data = error.response?.data as { message?: string } | undefined
    if (data?.message) return data.message
    return error.response ? '请求失败，请稍后重试' : '无法连接服务器，请确认后端 8080 已启动'
  }
  return '请求失败，请稍后重试'
}

export default client
