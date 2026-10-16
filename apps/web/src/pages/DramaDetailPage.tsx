import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { getApiErrorMessage } from '../api/client'
import { formatLikes, getDrama, type DramaDetail } from '../api/dramas'
import { useDramaInteractions } from '../hooks/useDramaInteractions'

type LoadState = 'loading' | 'ready' | 'error'

export default function DramaDetailPage() {
  const { id } = useParams()
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

  return (
    <main className="mx-auto w-full max-w-5xl px-4 py-6">
      {state === 'loading' && <p className="py-20 text-center text-sm text-neutral-400">加载中…</p>}

      {state === 'error' && (
        <div className="py-20 text-center">
          <p className="text-sm text-red-400">{error}</p>
          <Link
            to="/"
            className="mt-3 inline-block rounded-lg border border-neutral-700 px-4 py-1.5 text-sm text-neutral-300 transition hover:border-neutral-500 hover:text-white"
          >
            回首页
          </Link>
        </div>
      )}

      {state === 'ready' && detail && (
        <div className="flex flex-col gap-6 md:flex-row">
          <div className="mx-auto w-44 shrink-0 md:mx-0 md:w-52">
            <img
              src={detail.coverUrl}
              alt={detail.title}
              className="aspect-[9/16] w-full rounded-xl object-cover"
            />
          </div>

          <section className="min-w-0 flex-1">
            <h1 className="text-2xl font-bold">{detail.title}</h1>
            <div className="mt-2 flex flex-wrap items-center gap-2 text-sm text-neutral-400">
              <span className="rounded-full bg-rose-500/15 px-2 py-0.5 text-rose-400">{detail.category}</span>
              <span>全 {detail.episodeCount} 集</span>
              <span className="text-rose-300">♥ {formatLikes(detail.likeCount)}</span>
            </div>
            <p className="mt-4 text-sm leading-6 text-neutral-300">{detail.description}</p>

            <div className="mt-5 flex items-center gap-3">
              <button
                onClick={like}
                className={
                  detail.liked
                    ? 'rounded-lg bg-rose-500 px-5 py-2 text-sm font-medium text-white transition hover:bg-rose-600'
                    : 'rounded-lg border border-neutral-700 px-5 py-2 text-sm text-neutral-200 transition hover:border-rose-500 hover:text-rose-300'
                }
              >
                ♥ {detail.liked ? '已赞' : '点赞'}
              </button>
              <button
                onClick={favorite}
                className={
                  detail.favorited
                    ? 'rounded-lg bg-amber-500 px-5 py-2 text-sm font-medium text-white transition hover:bg-amber-600'
                    : 'rounded-lg border border-neutral-700 px-5 py-2 text-sm text-neutral-200 transition hover:border-amber-500 hover:text-amber-300'
                }
              >
                ★ {detail.favorited ? '已收藏' : '收藏'}
              </button>
            </div>
            {hint && <p className="mt-2 text-sm text-neutral-400">{hint}</p>}

            <h2 className="mt-8 text-base font-semibold">选集（共 {detail.episodeCount} 集）</h2>
            <div className="mt-3 grid grid-cols-4 gap-2 sm:grid-cols-6 md:grid-cols-8">
              {detail.episodes.map((episode) => (
                <Link
                  key={episode.id}
                  to={`/drama/${detail.id}/play?ep=${episode.episodeNo}`}
                  title={episode.title}
                  className="rounded-lg border border-neutral-700 bg-neutral-900 py-2 text-center text-sm text-neutral-200 transition hover:border-rose-500 hover:text-rose-300"
                >
                  {episode.episodeNo}
                </Link>
              ))}
            </div>
          </section>
        </div>
      )}
    </main>
  )
}
