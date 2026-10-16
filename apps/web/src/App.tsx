import { useEffect, useState } from 'react'
import axios from 'axios'

interface DramaSummary {
  id: number
  title: string
  description: string
  category: string
  coverUrl: string
  likeCount: number
  status: string
  episodeCount: number
}

type LoadState = 'loading' | 'ready' | 'error'

function formatLikes(count: number): string {
  return count >= 10000 ? `${(count / 10000).toFixed(1)}万` : count.toLocaleString()
}

export default function App() {
  const [dramas, setDramas] = useState<DramaSummary[]>([])
  const [state, setState] = useState<LoadState>('loading')

  useEffect(() => {
    axios
      .get<DramaSummary[]>('/api/dramas')
      .then((res) => {
        setDramas(res.data)
        setState('ready')
      })
      .catch(() => setState('error'))
  }, [])

  return (
    <div className="min-h-screen bg-neutral-950 text-white">
      <header className="sticky top-0 z-10 border-b border-neutral-800 bg-neutral-950/90 backdrop-blur">
        <div className="mx-auto flex h-14 max-w-6xl items-center px-4">
          <h1 className="text-lg font-bold">短剧</h1>
          <span className="ml-3 rounded-full bg-rose-500/15 px-2 py-0.5 text-xs text-rose-400">推荐</span>
        </div>
      </header>

      <main className="mx-auto max-w-6xl px-4 py-5">
        {state === 'loading' && <p className="py-20 text-center text-sm text-neutral-400">加载中…</p>}
        {state === 'error' && (
          <p className="py-20 text-center text-sm text-red-400">
            无法加载短剧列表（请确认后端 8080 已启动）
          </p>
        )}
        {state === 'ready' && (
          <div className="grid grid-cols-2 gap-3 sm:grid-cols-3 md:grid-cols-4 xl:grid-cols-6">
            {dramas.map((drama) => (
              <article key={drama.id} className="group cursor-pointer overflow-hidden rounded-xl bg-neutral-900">
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
              </article>
            ))}
          </div>
        )}
      </main>
    </div>
  )
}
