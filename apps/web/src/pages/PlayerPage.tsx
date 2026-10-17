import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { Link, useParams, useSearchParams } from 'react-router-dom'
import { getApiErrorMessage } from '../api/client'
import { formatLikes, getDrama, type DramaDetail } from '../api/dramas'
import { getHistory, reportProgress } from '../api/progress'
import { useDramaInteractions } from '../hooks/useDramaInteractions'
import { useAuth } from '../store/AuthContext'

type LoadState = 'loading' | 'ready' | 'error'

const REPORT_INTERVAL_MS = 5000
const WHEEL_THROTTLE_MS = 500
const TOUCH_SWIPE_PX = 50
const RESUME_TAIL_GRACE_SEC = 3

export default function PlayerPage() {
  const { id } = useParams()
  const [searchParams, setSearchParams] = useSearchParams()
  const [detail, setDetail] = useState<DramaDetail | null>(null)
  const [state, setState] = useState<LoadState>('loading')
  const [error, setError] = useState('')
  const { like, favorite, hint } = useDramaInteractions()
  const { user } = useAuth()

  const videoRef = useRef<HTMLVideoElement>(null)
  // 手势调度状态走 ref，避免每次 timeupdate/wheel 重渲染
  const lastReportAtRef = useRef(0)
  const resumeSecRef = useRef<number | null>(null)
  const resumedForRef = useRef<number | null>(null)
  const pendingAutoPlayRef = useRef(false)
  const lastWheelAtRef = useRef(0)
  const touchStartYRef = useRef<number | null>(null)

  const [notice, setNotice] = useState('')
  const [resumeTip, setResumeTip] = useState('')
  const [autoplayBlocked, setAutoplayBlocked] = useState(false)

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
  const currentIndex = episodes.findIndex((episode) => episode.episodeNo === current?.episodeNo)

  // 提示淡出：切集边界 1.5s，续播提示 3s（PL-04 演示时可指着讲）
  useEffect(() => {
    if (!notice) return
    const timer = setTimeout(() => setNotice(''), 1500)
    return () => clearTimeout(timer)
  }, [notice])
  useEffect(() => {
    if (!resumeTip) return
    const timer = setTimeout(() => setResumeTip(''), 3000)
    return () => clearTimeout(timer)
  }, [resumeTip])

  /** 立即上报当前集进度（暂停/切集/隐藏/卸载补报共用）；未登录零调用。 */
  const reportCurrent = useCallback(() => {
    const video = videoRef.current
    if (!user || !detail || !current || !video) return
    const sec = Math.floor(video.currentTime)
    if (sec <= 0) return
    reportProgress(detail.id, current.id, sec).catch(() => {})
  }, [user, detail, current])

  const selectEpisode = useCallback(
    (episodeNo: number) => {
      reportCurrent() // 切集前补报旧集进度（同步读 currentTime，此时 video 还是旧集）
      pendingAutoPlayRef.current = false
      setAutoplayBlocked(false)
      setSearchParams({ ep: String(episodeNo) })
    },
    [reportCurrent, setSearchParams],
  )

  const goNext = useCallback(() => {
    if (currentIndex < 0 || currentIndex >= episodes.length - 1) {
      setNotice('已经是最后一集')
      return
    }
    selectEpisode(episodes[currentIndex + 1].episodeNo)
  }, [currentIndex, episodes, selectEpisode])

  const goPrev = useCallback(() => {
    if (currentIndex <= 0) {
      setNotice('已经是第一集')
      return
    }
    selectEpisode(episodes[currentIndex - 1].episodeNo)
  }, [currentIndex, episodes, selectEpisode])

  // 记忆续播（PL-04）：已登录且 ?ep= 缺省时恢复最近进度；URL 显式存在则跳过（用户选集优先于记忆）
  useEffect(() => {
    if (!user || !detail || episodes.length === 0) return
    if (resumedForRef.current === detail.id) return
    resumedForRef.current = detail.id
    if (searchParams.has('ep')) return
    getHistory()
      .then((items) => {
        const record = items.find((item) => item.dramaId === detail.id)
        if (!record) return
        resumeSecRef.current = record.progressSec
        setSearchParams({ ep: String(record.episodeNo) }, { replace: true })
        setResumeTip(`已为你续播到第 ${record.episodeNo} 集`)
      })
      .catch(() => {})
  }, [user, detail, episodes, searchParams, setSearchParams])

  // 页面隐藏/卸载补报：关标签页、切走前保住最后几秒进度
  useEffect(() => {
    const onVisibilityChange = () => {
      if (document.visibilityState === 'hidden') {
        reportCurrent()
      }
    }
    document.addEventListener('visibilitychange', onVisibilityChange)
    return () => {
      document.removeEventListener('visibilitychange', onVisibilityChange)
      reportCurrent()
    }
  }, [reportCurrent])

  // 键盘切集（PL-01）：↓/→ 下一集，↑/← 上一集
  useEffect(() => {
    const onKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'ArrowDown' || event.key === 'ArrowRight') {
        event.preventDefault()
        goNext()
      } else if (event.key === 'ArrowUp' || event.key === 'ArrowLeft') {
        event.preventDefault()
        goPrev()
      }
    }
    window.addEventListener('keydown', onKeyDown)
    return () => window.removeEventListener('keydown', onKeyDown)
  }, [goNext, goPrev])

  // 播放中 5s 节流上报（PL-04）
  const handleTimeUpdate = () => {
    const now = Date.now()
    if (now - lastReportAtRef.current < REPORT_INTERVAL_MS) return
    lastReportAtRef.current = now
    reportCurrent()
  }

  /** metadata 就绪后应用续播偏移（距片尾 <3s 视为已看完，从头播）+ 连播兜底。 */
  const handleLoadedMetadata = () => {
    const video = videoRef.current
    if (!video || !current) return
    if (resumeSecRef.current != null) {
      const sec = resumeSecRef.current
      resumeSecRef.current = null
      video.currentTime = current.durationSec - sec < RESUME_TAIL_GRACE_SEC ? 0 : sec
    }
    if (pendingAutoPlayRef.current) {
      pendingAutoPlayRef.current = false
      video.play().catch(() => setAutoplayBlocked(true))
    }
  }

  /** 自动连播（PL-02）：非末集自动进下一集；末集停留并提示。 */
  const handleEnded = () => {
    reportCurrent()
    if (currentIndex < 0 || currentIndex >= episodes.length - 1) {
      setNotice('已看完最后一集')
      return
    }
    pendingAutoPlayRef.current = true
    selectEpisode(episodes[currentIndex + 1].episodeNo)
  }

  // 滚轮切集（PL-01）：向下=下一集，节流 500ms
  const handleWheel = (event: React.WheelEvent) => {
    const now = Date.now()
    if (now - lastWheelAtRef.current < WHEEL_THROTTLE_MS) return
    if (event.deltaY === 0) return
    lastWheelAtRef.current = now
    if (event.deltaY > 0) goNext()
    else goPrev()
  }

  // 触摸滑动切集（PL-01）：纵向位移 >50px，上滑下一集、下滑上一集
  const handleTouchStart = (event: React.TouchEvent) => {
    touchStartYRef.current = event.touches[0]?.clientY ?? null
  }
  const handleTouchEnd = (event: React.TouchEvent) => {
    const startY = touchStartYRef.current
    touchStartYRef.current = null
    if (startY == null) return
    const endY = event.changedTouches[0]?.clientY
    if (endY == null) return
    const delta = startY - endY
    if (Math.abs(delta) < TOUCH_SWIPE_PX) return
    if (delta > 0) goNext()
    else goPrev()
  }

  const resumePlayback = () => {
    setAutoplayBlocked(false)
    videoRef.current?.play().catch(() => {})
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
        <div
          className="relative aspect-[9/16] max-h-[calc(100dvh-9rem)] w-full overflow-hidden bg-black md:rounded-xl"
          onWheel={handleWheel}
          onTouchStart={handleTouchStart}
          onTouchEnd={handleTouchEnd}
        >
          {/* key 随集切换强制重载 video 元素 */}
          <video
            key={current.id}
            ref={videoRef}
            src={current.videoUrl}
            controls
            autoPlay
            playsInline
            onTimeUpdate={handleTimeUpdate}
            onPause={reportCurrent}
            onEnded={handleEnded}
            onLoadedMetadata={handleLoadedMetadata}
            className="h-full w-full object-contain"
          />
          {autoplayBlocked && (
            <button
              type="button"
              onClick={resumePlayback}
              className="absolute inset-0 flex flex-col items-center justify-center gap-2 bg-black/60 text-sm text-white"
            >
              <span className="text-3xl">▶</span>
              继续下一集
            </button>
          )}
          {notice && (
            <p className="pointer-events-none absolute inset-x-0 top-0 bg-black/60 py-1.5 text-center text-sm text-white">
              {notice}
            </p>
          )}
          {resumeTip && (
            <p className="pointer-events-none absolute inset-x-0 bottom-12 bg-black/60 py-1.5 text-center text-sm text-white">
              {resumeTip}
            </p>
          )}
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
