import client from './client'

// ---------- 认证（与 C 端同一接口，后台侧校验 ADMIN 角色） ----------

export interface UserInfo {
  id: number
  username: string
  nickname: string
  role: 'USER' | 'ADMIN'
}

export interface AuthResponse {
  token: string
  user: UserInfo
}

export function login(username: string, password: string): Promise<AuthResponse> {
  return client.post<AuthResponse>('/api/auth/login', { username, password }).then((res) => res.data)
}

export function getMe(): Promise<UserInfo> {
  return client.get<UserInfo>('/api/auth/me', { skipAuthRedirect: true }).then((res) => res.data)
}

// ---------- 短剧管理 ----------

export interface AdminDramaRow {
  id: number
  title: string
  category: string
  episodeCount: number
  status: string
  likeCount: number
  createdAt: string
  coverUrl: string
}

export interface AdminDramaDetail {
  id: number
  title: string
  description: string
  category: string
  coverUrl: string
  status: string
}

export interface DramaSaveRequest {
  title: string
  description: string
  category: string
  coverUrl: string
  status: string
}

export interface PageResult<T> {
  content: T[]
  page: number
  size: number
  total: number
}

export function listDramas(page: number, size: number, keyword?: string): Promise<PageResult<AdminDramaRow>> {
  return client
    .get<PageResult<AdminDramaRow>>('/api/admin/dramas', {
      params: { page, size, keyword: keyword || undefined },
    })
    .then((res) => res.data)
}

export function getAdminDrama(id: number | string): Promise<AdminDramaDetail> {
  return client.get<AdminDramaDetail>(`/api/admin/dramas/${id}`).then((res) => res.data)
}

export function createDrama(request: DramaSaveRequest): Promise<AdminDramaRow> {
  return client.post<AdminDramaRow>('/api/admin/dramas', request).then((res) => res.data)
}

export function updateDrama(id: number | string, request: DramaSaveRequest): Promise<AdminDramaRow> {
  return client.put<AdminDramaRow>(`/api/admin/dramas/${id}`, request).then((res) => res.data)
}

export function deleteDrama(id: number | string): Promise<void> {
  return client.delete(`/api/admin/dramas/${id}`).then(() => undefined)
}

// ---------- 剧集管理 ----------

export interface EpisodeRow {
  id: number
  episodeNo: number
  title: string
  videoUrl: string
  durationSec: number
}

export interface EpisodeSaveRequest {
  episodeNo: number
  title: string
  videoUrl: string
  durationSec: number
}

export function listEpisodes(dramaId: number | string): Promise<EpisodeRow[]> {
  return client.get<EpisodeRow[]>(`/api/admin/dramas/${dramaId}/episodes`).then((res) => res.data)
}

export function createEpisode(dramaId: number | string, request: EpisodeSaveRequest): Promise<EpisodeRow> {
  return client.post<EpisodeRow>(`/api/admin/dramas/${dramaId}/episodes`, request).then((res) => res.data)
}

export function updateEpisode(episodeId: number | string, request: EpisodeSaveRequest): Promise<EpisodeRow> {
  return client.put<EpisodeRow>(`/api/admin/episodes/${episodeId}`, request).then((res) => res.data)
}

export function deleteEpisode(episodeId: number | string): Promise<void> {
  return client.delete(`/api/admin/episodes/${episodeId}`).then(() => undefined)
}

// ---------- 文件上传 ----------

/** 上传封面/视频，返回 /uploads/files/<uuid>.<ext>。 */
export function uploadFile(file: File | Blob, type: 'cover' | 'video'): Promise<string> {
  const form = new FormData()
  form.append('file', file)
  form.append('type', type)
  return client.post<{ url: string }>('/api/admin/files/upload', form).then((res) => res.data.url)
}
