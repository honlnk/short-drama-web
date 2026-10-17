import client from './client'

/** 观看历史条目（PL-05）：后端 HistoryItem 平铺，updatedAt 为 ISO 字符串。 */
export interface HistoryItem {
  dramaId: number
  title: string
  description: string
  category: string
  coverUrl: string
  likeCount: number
  status: string
  episodeCount: number
  episodeId: number
  episodeNo: number
  episodeTitle: string
  progressSec: number
  updatedAt: string
}

/** 进度上报（PL-04）：每用户每剧 upsert 一条；失败静默，不打扰观看。 */
export function reportProgress(dramaId: number, episodeId: number, progressSec: number): Promise<unknown> {
  return client
    .put('/api/me/progress', { episodeId, progressSec }, { params: { dramaId } })
    .then((res) => res.data)
}

/** 观看历史（PL-04 续播数据源 + PL-05）：最近观看在前。 */
export function getHistory(): Promise<HistoryItem[]> {
  return client.get<HistoryItem[]>('/api/me/history').then((res) => res.data)
}
