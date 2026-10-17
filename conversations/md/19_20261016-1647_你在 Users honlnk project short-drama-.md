# 你在 /Users/honlnk/project/short-drama-web 仓库做只读代码 review（阶...

- 会话 ID：`sess_subagent_agent_3ec05f81-a224-4a5a-9b8c-dd8143f8736e`
- 类型：subagent_child（子代理会话，由父会话派生）
- 创建时间：2026-10-16 16:47:08　最后活动：2026-10-16 16:50:04
- 消息总数：10

> 本文件仅记录用户发送的内容与 AI 每轮的最终回复；执行过程（工具调用、中间推理）见 json/ 目录同名全量文件。

---

## 第 1 轮 · 2026-10-16 16:47:08

### 👤 用户

你在 /Users/honlnk/project/short-drama-web 仓库做只读代码 review（阶段2 C 端前端：React 路由 + 登录态 + 详情/播放页）。计划文档在 docs/plan/第二阶段-用户链路与核心演示路径.md，可先读了解契约（localStorage key=token、React Context、?ep= 同步、三档响应式等）。

请逐个完整读以下文件（不要跳读，读全文）：
- apps/web/src/api/client.ts
- apps/web/src/api/auth.ts
- apps/web/src/api/dramas.ts
- apps/web/src/store/AuthContext.tsx
- apps/web/src/router/index.tsx
- apps/web/src/components/Layout.tsx
- apps/web/src/components/RequireAuth.tsx
- apps/web/src/hooks/useDramaInteractions.ts
- apps/web/src/pages/HomePage.tsx
- apps/web/src/pages/DramaDetailPage.tsx
- apps/web/src/pages/PlayerPage.tsx
- apps/web/src/pages/LoginPage.tsx
- apps/web/src/pages/RegisterPage.tsx
- apps/web/src/App.tsx
- apps/web/vite.config.ts（代理配置）
- apps/web/src/index.css 或 tailwind 入口（确认 Tailwind v4 接入方式）

要核实的问题（逐条给出结论 + 证据行号）：
1. axios client：token 怎么塞？401 拦截做了什么（清 token？跳登录？from 回跳？）？skipAuthRedirect 机制怎么用？有没有拦截器泄漏（response 拦截器里 reject 后错误格式）？
2. AuthContext：启动时怎么恢复登录态（getMe？失败静默降级？）？token 存取 key 是否是 'token'？login/logout 流程？
3. 路由：五条路由 + 兜底？RequireAuth 实现（未登录跳哪？带回跳参数吗）？
4. DramaDetailPage：点赞收藏按钮未登录点击行为（跳登录带回跳？）、已登录点击行为（占位提示？不写库？）？状态回显来自接口？
5. PlayerPage：?ep= 同步逻辑（URL → state、切换集数时 URL 更新？刷新保持？）、video key 重载、响应式布局（PC 双栏？）、时长/选集 UI？
6. HomePage：分类筛选实现（前端过滤还是后端参数？）、响应式网格（几列断点）？loading/error/空态？
7. LoginPage/RegisterPage：表单校验、错误提示展示（来自后端 message？）、注册即登录？快捷填充 demo/admin？
8. Layout：顶栏登录态显示/退出？
9. 有没有任何写死的业务数据（违反演示红线）？有没有外网资源引用（断网演示安全）？
10. 任何你发现的 bug、与计划不符的偏差、或体验问题。

输出：按上述 10 点组织，每条给结论 + 文件:行号 证据 + 严重度评级（阻塞/建议/提示）。发现的其他问题单列一节。

### 🤖 AI

已读完全部目标文件，信息足够，下面输出 review 结论。

---

# 阶段 2 C 端前端 Review 报告

## 1. axios client（token 注入 / 401 拦截 / skipAuthRedirect / reject 格式）

**结论：实现正确，符合契约。**

- token 注入：`client.ts:15-21` 请求拦截器从 `localStorage.getItem(TOKEN_KEY)` 读取并塞 `config.headers.Authorization = Bearer <token>`。
- 401 拦截：`client.ts:23-35` 清 token（`localStorage.removeItem`，行 27）→ 跳 `/login?from=<encodeURIComponent(pathname+search)>`（行 28-30），from 参数包含 query。
- skipAuthRedirect：`client.ts:5-10` 通过 `declare module 'axios'` 扩展 `AxiosRequestConfig`，行 26 处条件 `!error.config?.skipAuthRedirect` 判断；唯一使用点在 `api/auth.ts:27` 的 `getMe()`。
- 拦截器泄漏：`client.ts:33` 始终 `Promise.reject(error)`，未吞错也未改错格式；axios 错误向上抛，业务侧由 `getApiErrorMessage`（行 38-45）统一提取。
- 登录页特殊处理：行 29 `if (!pathname.startsWith('/login'))` 避免在登录页失败时再次跳登录页死循环 —— 是好的，**但**未排除 `/register`（见第 10 条）。

严重度：无阻塞。

## 2. AuthContext（启动恢复 / token key / login / logout）

**结论：符合计划，启动恢复实现干净。**

- token key：统一常量 `TOKEN_KEY = 'token'`（`client.ts:3`），AuthContext 与 client 都引用它（`AuthContext.tsx:3, 18, 22, 38, 44`），不会出现 magic string 漂移。
- 启动恢复：`AuthContext.tsx:21-35` `useEffect` 挂载时如有 token 调 `getMe()` 校验；成功 `setUser`，失败 catch 中清 token + `setToken(null)`（行 30-33），静默降级为访客，不打断当前页（`getMe` 走 `skipAuthRedirect: true`，所以不会触发 401 跳登录）；`finally` 里 `setReady(true)`。
- login：`AuthContext.tsx:37-41` 写 localStorage + 双 setState。
- logout：`AuthContext.tsx:43-47` 清 localStorage + 双清 state。
- ready 标记：`AuthContext.tsx:9, 19, 24, 34`，`Layout.tsx:23` 用 `!ready ? null` 防止顶栏闪烁，`RequireAuth.tsx:13` 在 `!ready` 时返回 null 避免误跳。

严重度：无阻塞。**提示**：useState 初始化时读了 localStorage（行 18），随后 useEffect 又读一次（行 22）—— 是冗余读取，不是 bug。

## 3. 路由（5 条 + 兜底 / RequireAuth）

**结论：5 条路由 + `*` 兜底，RequireAuth 实现正确但本阶段未挂载（符合计划）。**

- 路由表：`router/index.tsx:9-21`，`/`、`/drama/:id`、`/drama/:id/play`、`/login`、`/register`、`* → <Navigate to="/" replace>`。
- 所有路由包在 `Layout` 里（行 10-12），共享顶栏。
- `RequireAuth.tsx:9-21`：`!ready` 返回 null；未登录 `<Navigate to={/login?from=${encodeURIComponent(pathname+search)}} replace />`；已登录渲染 children。
- 计划文档 124 行明确：「守卫组件先备好供里程碑 4 用」「本阶段点赞收藏用『点击时判断』而非整页守卫」—— 当前未挂载到路由是**符合计划**的，不是缺陷。

严重度：无阻塞。

## 4. DramaDetailPage（点赞/收藏未登录行为 / 已登录行为 / 状态回显）

**结论：符合计划。**

- 状态回显来自接口：`DramaDetailPage.tsx:22` `getDrama(id)` → 行 77, 82, 87, 92 用 `detail.liked` / `detail.favorited` 渲染按钮文案与样式（已赞/点赞、已收藏/收藏）。
- 未登录点击：`useDramaInteractions.ts:22-31` 的 `guarded` 函数，无 user 时 `navigate(/login?from=${encodeURIComponent(pathname+search)})`，带回跳。
- 已登录点击：同 hook 行 29 `setHint(message)`，message 来自 `like` / `favorite`（行 34-35）：「点赞功能即将上线，敬请期待」「收藏功能即将上线，敬请期待」；hint 在 2500ms 后自动清空（行 14-20）。**不写库** —— 与计划 195 行「按钮仅 UI + 未登录引导，写库属里程碑 4」一致。
- hint 展示：`DramaDetailPage.tsx:95`。

严重度：无阻塞。**提示**：hint 是页面级状态，从详情页跳到播放页再回来 hint 会重置（这是合理行为）。

## 5. PlayerPage（?ep= 同步 / video key / 响应式 / 时长）

**结论：核心逻辑正确，但 videoUrl 数据来源与计划略有偏差。**

- ?ep= 同步：
  - URL → state：`PlayerPage.tsx:11` `useSearchParams()`，行 41 `Number(searchParams.get('ep'))`，行 42 `episodes.find(...) ?? episodes[0]` 兜底第 1 集。
  - 切集 → URL：行 44-46 `setSearchParams({ ep: String(episodeNo) })`（覆盖式 set，不保留其它参数 —— 本页面没其它参数所以 OK）。
  - 刷新保持：URL 是唯一 source of truth，刷新后从 URL 恢复，正确。
  - **注意**：`setSearchParams` 默认是 push 而非 replace —— 每次切集都会增加一条历史记录，用户点浏览器返回会逐集倒退。这是常见行为但可能不是想要的（见第 10 条）。
- video key 重载：行 77 `key={current.id}` 切换集时强制重建 video 元素，配 `autoPlay`（行 80）会自动播放新集。
- 响应式：行 71 `md:flex-row md:items-start md:justify-center` PC 双栏；行 73 视频容器 `w-full max-w-[400px] md:w-[400px]`；行 74 `aspect-[9/16] max-h-[calc(100dvh-9rem)] overflow-hidden bg-black md:rounded-xl`；行 91 aside `w-full max-w-[400px] md:w-80`。
- 时长：行 86 `{current.durationSec}s` 直接以秒展示，未格式化为 mm:ss（体验问题，见第 10 条）。
- **数据来源偏差**：`PlayerPage.tsx:23` 调 `getDrama(id)`（即 `/api/dramas/{id}`），从 `detail.episodes` 取列表（行 39），**未调用** `listEpisodes`（`/api/dramas/{id}/episodes`）。计划文档 149 行写「`GET /api/dramas/{id}/episodes`（+详情取剧名）」—— 实施用的是「详情一次性返回 episodes」，效果等价且少一次请求，**实质更优**，但与计划字面不符（见第 10 条）。
- **冗余**：`api/dramas.ts:37-39` `listEpisodes` 已定义但未被任何页面调用（死代码）。

## 6. HomePage（分类筛选 / 响应式网格 / 三态）

**结论：符合计划。**

- 分类筛选：`HomePage.tsx:35-39` 完全前端过滤；分类条动态生成 `['全部', ...去重(drama.category)]` —— 比计划文档 137 行「全部/甜宠/都市/逆袭/古装/悬疑」硬编码列表**更灵活**，种子数据改分类不用改前端，符合「不写死」原则。是改进不是偏差。
- 响应式网格：行 78 `grid grid-cols-2 gap-3 md:grid-cols-4 xl:grid-cols-6` —— 严格匹配计划「手机 2 列 / 平板 4 列 / PC 6 列」。
- loading：行 43；error + 重试：行 45-55（`setReloadKey` 触发 useEffect 重跑）；空态：行 75-77「该分类暂无短剧」。
- 取消机制：行 16-31 用 `cancelled` flag 防止 setState after unmount。

严重度：无阻塞。

## 7. LoginPage / RegisterPage

**结论：符合计划。**

- LoginPage：
  - 空值校验：`LoginPage.tsx:19-22`。
  - 错误展示：行 30 `setError(getApiErrorMessage(err))`，行 69 渲染 —— 走后端 `{code,message}` 的 message 字段（`client.ts:40-41`）。
  - 成功跳回：行 15 `from = searchParams.get('from') || '/'`，行 28 `navigate(from, { replace: true })`。
  - 快捷填充：行 79-101 demo / demo123 与 admin / admin123 两个按钮。
  - **未发现 from 参数开放跳转校验**：`navigate(from)` 直接用 query string 的值，若 `from=https://evil.com`，react-router v7 默认会当作外部 URL 处理吗？react-router 的 navigate 对绝对 URL 默认仍走 client-side（path 解析失败时会 404），**但**更稳妥是只允许 `/` 开头的 path。严重度：建议。
- RegisterPage：
  - 用户名长度 2~50 校验：`RegisterPage.tsx:19-22`。
  - 密码非空：行 23-26。
  - 二次确认一致：行 27-30。
  - 注册即登录：行 35-37 成功直接 `login(token, user)` + `navigate('/', { replace: true })` —— 与计划 46 行「注册成功直接返回 JWT，前端自动进入登录态」一致。
  - 链接到登录：行 102-107。

严重度：无阻塞。**提示**：RegisterPage 未读 `?from=`，注册后固定回首页 —— 计划 129 行就是这么定的「成功后自动登录跳首页」，OK。

## 8. Layout（顶栏登录态 / 退出）

**结论：符合。**

- 登录态显示：`Layout.tsx:23-32` 显示 `user.nickname || user.username` + 退出按钮；未登录显示登录/注册链接（行 33-44）。
- ready 闪烁防护：行 23 `!ready ? null`。
- 退出：行 9-12 `logout()` + `navigate('/')`。

严重度：无阻塞。

## 9. 写死业务数据 / 外网资源引用

**结论：演示红线安全。**

- **业务数据**：grep 全 src，无硬编码剧名/封面/集数/点赞数。所有数据来自 `/api/dramas`、`/api/dramas/{id}`。
- **外网资源**：grep `https?://` 仅命中 `vite.config.ts:12-13`（开发代理目标 `http://localhost:8080`，合法）与第 5 行注释 URL。
- `index.html` 仅引用 `/favicon.svg`（本地）与 `/src/main.tsx`，无 CDN / 字体 / 外链。断网安全。

严重度：无阻塞。

## 10. Bug / 与计划偏差 / 体验问题（汇总）

### 阻塞
无。

### 建议

1. **`PlayerPage` 切集用 push 而非 replace**：`PlayerPage.tsx:45` `setSearchParams({ ep: String(episodeNo) })` 默认 push 历史。用户在第 1→2→3→4 集连点，浏览器后退需 4 次才回到详情页。建议改 `setSearchParams({ ep: String(episodeNo) }, { replace: true })`，与 `?ep=` 作为「当前状态」而非「导航历史」的语义更匹配。

2. **登录页 `from` 参数未做来源校验**：`LoginPage.tsx:15, 28` 直接 `navigate(from)`。虽然 react-router v7 对绝对 URL 会 fallback，仍建议加 `from.startsWith('/') ? from : '/'` 防御开放跳转。

3. **`listEpisodes` 函数定义但无调用方**：`api/dramas.ts:37-39`。PlayerPage 用的是 `getDrama` 里的 episodes。要么删函数，要么按计划文档 149 行字面改为「播放页调 listEpisodes + getDrama 取剧名」（但这会多一次请求）。**当前实施实际更优，建议反向更新计划文档**。

4. **`AuthContext` 中 token state 与 localStorage 双写**：`AuthContext.tsx:18` 初始化时从 localStorage 读，行 38 / 44 又各写 / 清一次。token 在 React 状态里其实从未被消费（仅 localStorage 被 axios 拦截器读取），是冗余 state。可简化为只在 localStorage 存，context 只暴露 user 与 login/logout。无功能影响。

5. **`client.ts` 401 处理未排除 `/register` 页**：行 29 `if (!pathname.startsWith('/login'))`。如果用户在 `/register` 提交时已持有的 token 失效（少见但可能），注册接口本身返回 401（理论上不会，注册是公开接口）—— 实际不会发生。这条只是思维演练，**提示级**。

### 提示

6. **播放页时长显示未格式化**：`PlayerPage.tsx:86` `{current.durationSec}s` 输出如「90s」。种子数据 90~120s 区间秒数还可读，但演示时「1:30」更友好。可写 `formatDuration` 工具。

7. **`PlayerPage` 当前集态不对齐父级 URL**：行 42 `episodes.find(...) ?? episodes[0]` 兜底到第 1 集，但 URL 仍保持 `?ep=99` 之类的非法值。可以在 detail 加载完成后用 effect 把 URL 改成合法值（`setSearchParams({ep:'1'}, {replace:true})`），避免刷新后永远卡在非法 ep 参数。

8. **`LoginPage`/`RegisterPage` 输入框无 `required` / `minLength` HTML 属性**：校验全靠 JS submit 函数。加上能让浏览器原生挡一层，且对无障碍更友好。

9. **`useDramaInteractions` 用 `window.location` 而非 `useLocation`**：`useDramaInteractions.ts:25`。功能等价，但与 `RequireAuth.tsx:11` 用 `useLocation()` 风格不一致。

10. **`Layout` 顶栏的「推荐」标签是硬编码装饰**：`Layout.tsx:21`「短剧」logo 旁的「推荐」胶囊永远是这两个字。属品牌装饰非业务数据，不违反红线，但严格来说不是从接口来 —— 提示级。

11. **PlayerPage 未消费 `episodes` memo 的必要性**：`PlayerPage.tsx:39` `useMemo(() => detail?.episodes ?? [], [detail])` —— detail 本身就已是 state，memo 这里没省什么。提示级。

---

## 总评

实现整体**高质量、严格符合计划**，所有 10 个核实点的核心契约（token key='token'、skipAuthRedirect、注册即登录、不写库占位、`?ep=` 同步、2/4/6 列响应式、无写死业务数据、无外网资源）全部满足。**0 阻塞，5 建议，6 提示**。最重要的两个改进点：(a) `setSearchParams` 加 `replace: true` 优化切集历史栈；(b) `LoginPage` 的 `from` 参数加白名单校验。

---
