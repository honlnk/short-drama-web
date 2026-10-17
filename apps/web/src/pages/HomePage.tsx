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
  const [keyword, setKeyword] = useState('')
  const [debouncedKeyword, setDebouncedKeyword] = useState('')

  // PL-06 搜索：输入 300ms 防抖后触发请求，避免每次击键打后端
  useEffect(() => {
    const timer = setTimeout(() => setDebouncedKeyword(keyword.trim()), 300)
    return () => clearTimeout(timer)
  }, [keyword])

  useEffect(() => {
    let cancelled = false
    setState('loading')
    listDramas(debouncedKeyword)
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
  }, [debouncedKeyword, reloadKey])

  // 分类条随数据生成（全部 + 去重分类）；keyword 走后端、category 走前端，两条件叠加
  const categories = useMemo(
    () => ['全部', ...Array.from(new Set(dramas.map((drama) => drama.category)))],
    [dramas],
  )
  const visible = category === '全部' ? dramas : dramas.filter((drama) => drama.category === category)

  const clearKeyword = () => {
    setKeyword('')
    setDebouncedKeyword('')
  }

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
          {/* 搜索框：手机独立一行，平板/PC 靠右（PL-06）。不清空分类状态，两条件正交 */}
          <div className="mb-4 flex md:justify-end">
            <div className="relative w-full md:w-72">
              <svg
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
                className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-neutral-500"
                aria-hidden="true"
              >
                <circle cx="11" cy="11" r="7" />
                <path d="m20 20-3.5-3.5" strokeLinecap="round" />
              </svg>
              {/* text 而非 search：WebKit 会给 search 输入框渲染原生清除按钮，与自定义清空按钮重叠导致点不中 */}
              <input
                type="text"
                value={keyword}
                onChange={(event) => setKeyword(event.target.value)}
                placeholder="搜索短剧"
                aria-label="搜索短剧"
                className="w-full rounded-full border border-neutral-700 bg-neutral-900 py-2 pl-9 pr-9 text-sm text-neutral-100 placeholder:text-neutral-500 outline-none transition focus:border-rose-500"
              />
              {keyword && (
                <button
                  type="button"
                  onClick={clearKeyword}
                  aria-label="清空搜索"
                  className="absolute right-2 top-1/2 flex h-6 w-6 -translate-y-1/2 items-center justify-center rounded-full text-neutral-400 transition hover:bg-neutral-800 hover:text-white"
                >
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" className="h-3.5 w-3.5">
                    <path d="M6 6l12 12M18 6 6 18" strokeLinecap="round" />
                  </svg>
                </button>
              )}
            </div>
          </div>

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
            <p className="py-20 text-center text-sm text-neutral-400">
              {debouncedKeyword ? '没有找到相关短剧' : '该分类暂无短剧'}
            </p>
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
