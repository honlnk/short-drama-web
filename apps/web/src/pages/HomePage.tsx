import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { getApiErrorMessage } from '../api/client'
import { formatLikes, listDramas, type DramaSummary } from '../api/dramas'

type LoadState = 'loading' | 'ready' | 'error'

export default function HomePage() {
  const [dramas, setDramas] = useState<DramaSummary[]>([])
  const [state, setState] = useState<LoadState>('loading')
  const [error, setError] = useState('')
  const [category, setCategory] = useState('全部')
  const [reloadKey, setReloadKey] = useState(0)

  useEffect(() => {
    let cancelled = false
    setState('loading')
    listDramas()
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
  }, [reloadKey])

  // 分类条随种子数据生成（全部 + 去重分类），数据量小走前端过滤
  const categories = useMemo(
    () => ['全部', ...Array.from(new Set(dramas.map((drama) => drama.category)))],
    [dramas],
  )
  const visible = category === '全部' ? dramas : dramas.filter((drama) => drama.category === category)

  return (
    <main className="mx-auto w-full max-w-6xl px-4 py-5">
      {state === 'loading' && <p className="py-20 text-center text-sm text-neutral-400">加载中…</p>}

      {state === 'error' && (
        <div className="py-20 text-center">
          <p className="text-sm text-red-400">{error}</p>
          <button
            onClick={() => setReloadKey((key) => key + 1)}
            className="mt-3 rounded-lg border border-neutral-700 px-4 py-1.5 text-sm text-neutral-300 transition hover:border-neutral-500 hover:text-white"
          >
            重试
          </button>
        </div>
      )}

      {state === 'ready' && (
        <>
          <nav className="mb-4 flex gap-2 overflow-x-auto pb-1" aria-label="分类筛选">
            {categories.map((item) => (
              <button
                key={item}
                onClick={() => setCategory(item)}
                className={`shrink-0 rounded-full px-3.5 py-1.5 text-sm transition ${
                  item === category
                    ? 'bg-rose-500 font-medium text-white'
                    : 'border border-neutral-700 bg-neutral-900 text-neutral-300 hover:border-neutral-500 hover:text-white'
                }`}
              >
                {item}
              </button>
            ))}
          </nav>

          {visible.length === 0 ? (
            <p className="py-20 text-center text-sm text-neutral-400">该分类暂无短剧</p>
          ) : (
            <div className="grid grid-cols-2 gap-3 md:grid-cols-4 xl:grid-cols-6">
              {visible.map((drama) => (
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
                    <span className="absolute bottom-1.5 right-1.5 rounded-md bg-black/55 px-1.5 py-0.5 text-xs text-rose-300">
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
          )}
        </>
      )}
    </main>
  )
}
