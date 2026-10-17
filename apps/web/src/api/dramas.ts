import client from './client'

export interface DramaSummary {
  id: number
  title: string
  description: string
  category: string
  coverUrl: string
  likeCount: number
  status: string
  episodeCount: number
}

export interface EpisodeSummary {
  id: number
  episodeNo: number
  title: string
  videoUrl: string
  durationSec: number
}

export interface DramaDetail extends DramaSummary {
  episodes: EpisodeSummary[]
  liked: boolean
  favorited: boolean
}

/** 首页列表/搜索（PL-06）：keyword 可选，空串/undefined 均返回全量。 */
export function listDramas(keyword?: string): Promise<DramaSummary[]> {
  const query = keyword && keyword.trim() ? `?keyword=${encodeURIComponent(keyword.trim())}` : ''
  return client.get<DramaSummary[]>(`/api/dramas${query}`).then((res) => res.data)
}

/** 详情：带 token 时 liked/favorited 反映当前用户真实状态，匿名恒 false。 */
export function getDrama(id: number | string): Promise<DramaDetail> {
  return client.get<DramaDetail>(`/api/dramas/${id}`).then((res) => res.data)
}

export function listEpisodes(id: number | string): Promise<EpisodeSummary[]> {
  return client.get<EpisodeSummary[]>(`/api/dramas/${id}/episodes`).then((res) => res.data)
}

export function formatLikes(count: number): string {
  return count >= 10000 ? `${(count / 10000).toFixed(1)}万` : count.toLocaleString()
}
