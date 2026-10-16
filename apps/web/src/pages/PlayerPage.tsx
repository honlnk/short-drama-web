import { useEffect, useMemo, useState } from 'react'
import { Link, useParams, useSearchParams } from 'react-router-dom'
import { getApiErrorMessage } from '../api/client'
import { formatLikes, getDrama, type DramaDetail } from '../api/dramas'
import { useDramaInteractions } from '../hooks/useDramaInteractions'

type LoadState = 'loading' | 'ready' | 'error'

export default function PlayerPage() {
  const { id } = useParams()
  const [searchParams, setSearchParams] = useSearchParams()
  const [detail, setDetail] = useState<DramaDetail | null>(null)
  const [state, setState] = useState<LoadState>('loading')
  const [error, setError] = useState('')
  const { like, favorite, hint } = useDramaInteractions()

  useEffect(() => {
    if (!id) {
      return
    }
    let cancelled = false
    setState('loading')
    getDrama(id)
      .then((data) => {
        if (cancelled) return
        setDetail(data)
        setState('ready')
      })
      .catch((err) => {
        if (cancelled) return
        setError(getApiErrorMessage(err))
        setState('error')
      })
    return () => {
      cancelled = true
    }
  }, [id])

  const episodes = useMemo(() => detail?.episodes ?? [], [detail])
  // ?ep= 定位当前集：缺失/非法时回落第 1 集
  const epParam = Number(searchParams.get('ep'))
  const current = episodes.find((episode) => episode.episodeNo === epParam) ?? episodes[0]

  const selectEpisode = (episodeNo: number) => {
    setSearchParams({ ep: String(episodeNo) })
  }

  if (state === 'loading') {
    return (
      <main className="flex flex-1 items-center justify-center">
        <p className="text-sm text-neutral-400">加载中…</p>
      </main>
    )
  }

  if (state === 'error' || !detail || !current) {
    return (
      <main className="flex flex-1 flex-col items-center justify-center gap-3">
        <p className="text-sm text-red-400">{state === 'error' ? error : '暂无剧集'}</p>
        <Link
          to="/"
          className="rounded-lg border border-neutral-700 px-4 py-1.5 text-sm text-neutral-300 transition hover:border-neutral-500 hover:text-white"
        >
          回首页
        </Link>
      </main>
    )
  }

  return (
    <main className="flex w-full flex-1 flex-col items-center gap-5 md:flex-row md:items-start md:justify-center md:gap-8 md:px-4 md:py-6">
      {/* 竖屏播放区：宽 min(100vw, 400px)，容器 9:16，视频 object-contain 不拉宽不裁切 */}
      <div className="w-full max-w-[400px] md:w-[400px]">
        <div className="aspect-[9/16] max-h-[calc(100dvh-9rem)] w-full overflow-hidden bg-black md:rounded-xl">
          {/* key 随集切换强制重载 video 元素 */}
          <video
            key={current.id}
            src={current.videoUrl}
            controls
            autoPlay
            playsInline
            className="h-full w-full object-contain"
          />
        </div>
        <p className="mt-2 px-4 text-center text-sm text-neutral-300 md:px-0">
          {current.title} · {current.durationSec}s
        </p>
      </div>

      {/* 信息栏：平板/PC 右栏，手机在视频下方 */}
      <aside className="w-full max-w-[400px] px-4 pb-8 md:w-80 md:shrink-0 md:px-0 md:pb-0">
        <h1 className="text-lg font-bold">{detail.title}</h1>
        <div className="mt-1.5 flex flex-wrap items-center gap-2 text-sm text-neutral-400">
          <span className="rounded-full bg-rose-500/15 px-2 py-0.5 text-rose-400">{detail.category}</span>
          <span>全 {detail.episodeCount} 集</span>
          <span className="text-rose-300">♥ {formatLikes(detail.likeCount)}</span>
        </div>

        <div className="mt-3 flex items-center gap-2.5">
          <button
            onClick={like}
            className={
              detail.liked
                ? 'rounded-lg bg-rose-500 px-4 py-1.5 text-sm font-medium text-white transition hover:bg-rose-600'
                : 'rounded-lg border border-neutral-700 px-4 py-1.5 text-sm text-neutral-200 transition hover:border-rose-500 hover:text-rose-300'
            }
          >
            ♥ {detail.liked ? '已赞' : '点赞'}
          </button>
          <button
            onClick={favorite}
            className={
              detail.favorited
                ? 'rounded-lg bg-amber-500 px-4 py-1.5 text-sm font-medium text-white transition hover:bg-amber-600'
                : 'rounded-lg border border-neutral-700 px-4 py-1.5 text-sm text-neutral-200 transition hover:border-amber-500 hover:text-amber-300'
            }
          >
            ★ {detail.favorited ? '已收藏' : '收藏'}
          </button>
        </div>
        {hint && <p className="mt-2 text-sm text-neutral-400">{hint}</p>}

        <p className="mt-3 text-sm leading-6 text-neutral-300">{detail.description}</p>

        <h2 className="mt-5 text-sm font-semibold">选集</h2>
        <div className="mt-2.5 grid grid-cols-6 gap-2">
          {detail.episodes.map((episode) => (
            <button
              key={episode.id}
              onClick={() => selectEpisode(episode.episodeNo)}
              className={
                episode.episodeNo === current.episodeNo
                  ? 'rounded-lg bg-rose-500 py-1.5 text-sm font-medium text-white'
                  : 'rounded-lg border border-neutral-700 bg-neutral-900 py-1.5 text-sm text-neutral-200 transition hover:border-rose-500 hover:text-rose-300'
              }
            >
              {episode.episodeNo}
            </button>
          ))}
        </div>

        <Link to={`/drama/${detail.id}`} className="mt-5 inline-block text-sm text-neutral-400 transition hover:text-white">
          ← 返回详情
        </Link>
      </aside>
    </main>
  )
}
