import client from './client'
import type { DramaSummary } from './dramas'

/** 我的收藏列表（IN-03）：按收藏时间倒序。 */
export function listMyFavorites(): Promise<DramaSummary[]> {
  return client.get<DramaSummary[]>('/api/me/favorites').then((res) => res.data)
}
