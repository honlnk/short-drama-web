import client from './client'

export type UserRole = 'USER' | 'ADMIN'

export interface UserInfo {
  id: number
  username: string
  nickname: string
  role: UserRole
}

export interface AuthResponse {
  token: string
  user: UserInfo
}

export function register(username: string, password: string): Promise<AuthResponse> {
  return client.post<AuthResponse>('/api/auth/register', { username, password }).then((res) => res.data)
}

export function login(username: string, password: string): Promise<AuthResponse> {
  return client.post<AuthResponse>('/api/auth/login', { username, password }).then((res) => res.data)
}

/** 启动时登录态校验：失败不清跳转，由 AuthContext 静默降级为访客。 */
export function getMe(): Promise<UserInfo> {
  return client.get<UserInfo>('/api/auth/me', { skipAuthRedirect: true }).then((res) => res.data)
}
