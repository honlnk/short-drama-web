import { useCallback, useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../store/AuthContext'

/**
 * 阶段2 点赞/收藏交互：仅 UI 状态回显 + 未登录引导登录，不写库（写库属里程碑 4）。
 * 返回的 like/favorite 供详情页与播放页共用。
 */
export function useDramaInteractions() {
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

  const guarded = useCallback(
    (message: string) => {
      if (!user) {
        const from = encodeURIComponent(window.location.pathname + window.location.search)
        navigate(`/login?from=${from}`)
        return
      }
      setHint(message)
    },
    [user, navigate],
  )

  const like = useCallback(() => guarded('点赞功能即将上线，敬请期待'), [guarded])
  const favorite = useCallback(() => guarded('收藏功能即将上线，敬请期待'), [guarded])

  return { like, favorite, hint }
}
