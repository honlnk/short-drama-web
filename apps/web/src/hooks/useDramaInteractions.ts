import { useCallback, useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { getApiErrorMessage } from '../api/client'
import { toggleFavorite, toggleLike } from '../api/dramas'
import { useAuth } from '../store/AuthContext'

export interface DramaInteractionState {
  id: number
  liked: boolean
  favorited: boolean
  likeCount: number
}

export type InteractionUpdate = Partial<Pick<DramaInteractionState, 'liked' | 'favorited' | 'likeCount'>>

/**
 * 点赞/收藏真实写库（阶段3）：乐观更新 + 响应校准，失败提示并回滚；
 * 未登录点击跳登录页并携带回跳。详情页与播放页共用。
 */
export function useDramaInteractions(
  drama: DramaInteractionState | null,
  onChange: (update: InteractionUpdate) => void,
) {
  const { user } = useAuth()
  const navigate = useNavigate()
  const [hint, setHint] = useState('')

  useEffect(() => {
    if (!hint) {
      return
    }
    const timer = setTimeout(() => setHint(''), 2500)
    return () => clearTimeout(timer)
  }, [hint])

  const requireLogin = useCallback(() => {
    if (user) {
      return true
    }
    const from = encodeURIComponent(window.location.pathname + window.location.search)
    navigate(`/login?from=${from}`)
    return false
  }, [user, navigate])

  const like = useCallback(() => {
    if (!drama || !requireLogin()) {
      return
    }
    const optimistic = { liked: !drama.liked, likeCount: drama.likeCount + (drama.liked ? -1 : 1) }
    onChange(optimistic)
    toggleLike(drama.id)
      .then((result) => onChange({ liked: result.liked, likeCount: result.likeCount }))
      .catch((err) => {
        onChange({ liked: drama.liked, likeCount: drama.likeCount })
        setHint(getApiErrorMessage(err))
      })
  }, [drama, requireLogin, onChange])

  const favorite = useCallback(() => {
    if (!drama || !requireLogin()) {
      return
    }
    onChange({ favorited: !drama.favorited })
    toggleFavorite(drama.id)
      .then((result) => onChange({ favorited: result.favorited }))
      .catch((err) => {
        onChange({ favorited: drama.favorited })
        setHint(getApiErrorMessage(err))
      })
  }, [drama, requireLogin, onChange])

  return { like, favorite, hint }
}
