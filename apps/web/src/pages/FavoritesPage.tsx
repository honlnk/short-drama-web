import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { getApiErrorMessage } from '../api/client'
import { formatLikes, type DramaSummary } from '../api/dramas'
import { listMyFavorites } from '../api/me'

type LoadState = 'loading' | 'ready' | 'error'

/** 我的收藏（IN-03）：卡片网格复用首页样式，按收藏时间倒序。路由层由 RequireAuth 守卫。 */
export default function FavoritesPage() {
  const [dramas, setDramas] = useState<DramaSummary[]>([])
  const [state, setState] = useState<LoadState>('loading')
  const [error, setError] = useState('')

  useEffect(() => {
    let cancelled = false
    setState('loading')
    listMyFavorites()
      .then((data) => {
        if (cancelled) return
        setDramas(data)
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
  }, [])

  return (
    <main className="mx-auto w-full max-w-6xl px-4 py-5">
      <h1 className="text-lg font-bold">我的收藏</h1>

      {state === 'loading' && <p className="py-20 text-center text-sm text-neutral-400">加载中…</p>}

      {state === 'error' && (
        <div className="py-20 text-center">
          <p className="text-sm text-red-400">{error}</p>
          <button
            onClick={() => window.location.reload()}
            className="mt-3 rounded-lg border border-neutral-700 px-4 py-1.5 text-sm text-neutral-300 transition hover:border-neutral-500 hover:text-white"
          >
            重试
          </button>
        </div>
      )}

      {state === 'ready' &&
        (dramas.length === 0 ? (
          <div className="py-20 text-center">
            <p className="text-sm text-neutral-400">还没有收藏，去首页逛逛</p>
            <Link
              to="/"
              className="mt-3 inline-block rounded-lg bg-rose-500 px-4 py-1.5 text-sm font-medium text-white transition hover:bg-rose-600"
            >
              回首页
            </Link>
          </div>
        ) : (
          <div className="mt-4 grid grid-cols-2 gap-3 md:grid-cols-4 xl:grid-cols-6">
            {dramas.map((drama) => (
              <Link
                key={drama.id}
                to={`/drama/${drama.id}`}
                className="group overflow-hidden rounded-xl bg-neutral-900 transition hover:ring-1 hover:ring-rose-500/60"
              >
                <div className="relative aspect-[9/16]">
                  <img
                    src={drama.coverUrl}
                    alt={drama.title}
                    loading="lazy"
                    className="h-full w-full object-cover transition group-hover:scale-105"
                  />
                  <span className="absolute left-1.5 top-1.5 rounded-md bg-black/55 px-1.5 py-0.5 text-xs">
                    {drama.category}
                  </span>
                  <span className="absolute bottom-1.5 right-1.5 rounded-md bg-black/55 px-1 py-0.5 text-xs text-rose-300">
                    ♥ {formatLikes(drama.likeCount)}
                  </span>
                </div>
                <div className="p-2">
                  <h2 className="truncate text-sm font-medium" title={drama.title}>
                    {drama.title}
                  </h2>
                  <p className="mt-0.5 text-xs text-neutral-400">全 {drama.episodeCount} 集</p>
                </div>
              </Link>
            ))}
          </div>
        ))}
    </main>
  )
}
