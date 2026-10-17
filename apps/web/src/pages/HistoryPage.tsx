import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { getApiErrorMessage } from '../api/client'
import { getHistory, type HistoryItem } from '../api/progress'

type LoadState = 'loading' | 'ready' | 'error'

/** 相对时间（本地格式化，不引第三方库）；超过 7 天回落 yyyy-MM-dd 绝对日期兜底。 */
function formatRelativeTime(iso: string): string {
  const then = new Date(iso).getTime()
  if (Number.isNaN(then)) return ''
  const minutes = Math.floor((Date.now() - then) / 60_000)
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes} 分钟前`
  if (minutes < 60 * 24) return `${Math.floor(minutes / 60)} 小时前`
  if (minutes < 60 * 24 * 7) return `${Math.floor(minutes / (60 * 24))} 天前`
  const d = new Date(then)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

/** 秒 → 「m 分 s 秒」，0 分显式保留（progressSec 为后端截断后的库值）。 */
function formatProgressSec(sec: number): string {
  return `${Math.floor(sec / 60)} 分 ${sec % 60} 秒`
}

/** 观看历史（PL-05）：横向条目卡，点击整卡进播放页（不带 ?ep=，由记忆续播恢复到记录的集数与秒数）。
 *  后端已按 updatedAt 倒序返回，此处不二次排序；OFFLINE 剧保留显示并叠「已下架」角标（历史是用户私有数据）。 */
export default function HistoryPage() {
  const [items, setItems] = useState<HistoryItem[]>([])
  const [state, setState] = useState<LoadState>('loading')
  const [error, setError] = useState('')

  useEffect(() => {
    let cancelled = false
    setState('loading')
    getHistory()
      .then((data) => {
        if (cancelled) return
        setItems(data)
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
    <main className="mx-auto w-full max-w-3xl px-4 py-5">
      <h1 className="text-lg font-bold">观看历史</h1>

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
        (items.length === 0 ? (
          <div className="py-20 text-center">
            <p className="text-sm text-neutral-400">还没有观看记录，去首页逛逛</p>
            <Link
              to="/"
              className="mt-3 inline-block rounded-lg bg-rose-500 px-4 py-1.5 text-sm font-medium text-white transition hover:bg-rose-600"
            >
              回首页
            </Link>
          </div>
        ) : (
          <ul className="mt-4 space-y-3">
            {items.map((item) => (
              <li key={item.dramaId}>
                <Link
                  to={`/drama/${item.dramaId}/play`}
                  className="flex gap-3 rounded-xl bg-neutral-900 p-2.5 transition hover:ring-1 hover:ring-rose-500/60"
                >
                  <div className="relative aspect-[9/16] w-16 shrink-0 overflow-hidden rounded-lg">
                    <img
                      src={item.coverUrl}
                      alt={item.title}
                      loading="lazy"
                      className="h-full w-full object-cover"
                    />
                    {item.status !== 'ONLINE' && (
                      <span className="absolute right-1 top-1 rounded bg-black/55 px-1 py-0.5 text-[10px] text-neutral-300">
                        已下架
                      </span>
                    )}
                  </div>
                  <div className="min-w-0 flex-1 py-0.5">
                    <h2 className="truncate text-sm font-medium" title={item.title}>
                      {item.title}
                    </h2>
                    <p className="mt-1.5 text-xs text-neutral-400">
                      看到第 {item.episodeNo} 集 · {formatProgressSec(item.progressSec)}
                    </p>
                    <p className="mt-1 text-xs text-neutral-500">{formatRelativeTime(item.updatedAt)}</p>
                  </div>
                </Link>
              </li>
            ))}
          </ul>
        ))}
    </main>
  )
}
