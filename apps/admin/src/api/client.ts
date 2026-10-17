import axios from 'axios'

export const TOKEN_KEY = 'admin_token'

declare module 'axios' {
  export interface AxiosRequestConfig {
    /** true 时 401 不触发跳转登录页（启动时登录态校验失败应静默降级）。 */
    skipAuthRedirect?: boolean
  }
}

/** 后台全站唯一 axios 实例：走 Vite 代理（/api、/uploads → 8080）。
 *  token 用独立 key admin_token，与 C 端（token）互不串登录态（同浏览器双开演示）。 */
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
    // 后端统一包体 {code,message,data}：此处统一拆包，页面层 res.data 即业务数据
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
      if (!window.location.pathname.startsWith('/login')) {
        window.location.assign('/login')
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
