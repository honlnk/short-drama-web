# 盘点 /Users/honlnk/project/short-drama-web 这个 monorepo 的实际实...

- 会话 ID：`sess_subagent_agent_1544cf48-b065-40e0-92f5-b33d339cd052`
- 类型：subagent_child（子代理会话，由父会话派生）
- 创建时间：2026-10-16 16:42:17　最后活动：2026-10-16 16:45:06
- 消息总数：15

> 本文件仅记录用户发送的内容与 AI 每轮的最终回复；执行过程（工具调用、中间推理）见 json/ 目录同名全量文件。

---

## 第 1 轮 · 2026-10-16 16:42:17

### 👤 用户

盘点 /Users/honlnk/project/short-drama-web 这个 monorepo 的实际实现情况（short-drama 短剧 Web APP，含 apps/api Spring Boot 后端、apps/web C 端 React、apps/admin 管理后台）。搜索广度：medium。

请核实并报告以下内容（给出文件路径佐证，不要贴大段代码）：

1. **后端 apps/api**：
   - entity/ 下有哪些实体，各自关键字段（尤其 Drama 的 status 取值、User 的 role）
   - controller/ 下实际暴露了哪些 HTTP 端点（方法+路径），逐个列出
   - service/、repository/ 下有哪些类
   - security/ 的 JWT 鉴权链路（JwtService/JwtAuthFilter/SecurityConfig 的鉴权规则：哪些路径放行、哪些需登录、哪些需 ADMIN）
   - config/DataSeeder 种子数据内容（几部剧、几集、几个账号）
   - 有没有测试类（src/test 下），测试覆盖什么
   - 有没有未实现/TODO/stub 的部分（比如点赞收藏写库接口是否存在）
2. **前端 apps/web**：
   - 路由有哪些（src 下 router/routes 定义），每个页面对应组件
   - api/client.ts 做了什么（token 注入、401 处理）
   - AuthContext 提供什么能力
   - 播放页实现了哪些交互（选集、播放进度续播、点赞收藏按钮目前是真请求还是纯 UI）
3. **管理后台 apps/admin**：
   - 目前实际有什么页面/功能，是骨架还是有真实功能
4. **scripts/ 和根目录**：有哪些脚本，各自作用
5. **docs/devlog.md**：最近几条日志记录了什么（最后 100 行左右即可）
6. 全局搜索 TODO/FIXME/占位 注释，列出位置

最后给一个简明结论：哪些需求（注册登录/短剧展示/点赞/收藏/管理后台）已真正落地打通，哪些还是 UI 或缺失。

### 🤖 AI

盘点完成。以下是核实后的实现情况报告。

# short-drama-web monorepo 实现盘点

## 1. 后端 apps/api（Spring Boot 3.5, Java 21）

### 实体（entity/，共 6 实体 + 2 枚举）
| 文件 | 关键点 |
|---|---|
| `/Users/honlnk/project/short-drama-web/apps/api/src/main/java/com/shortdrama/api/entity/Drama.java` | id/title/description/category/coverUrl/**likeCount**/status/createdAt + `@OneToMany episodes` |
| `entity/DramaStatus.java` | 枚举仅 `ONLINE` / `OFFLINE`（devlog 记录：取 PRD 口径，弃用 PUBLISHED） |
| `entity/User.java` | username(唯一)/password(BCrypt)/nickname/**role**/createdAt，表名反引号 `` `user` `` 避保留字 |
| `entity/UserRole.java` | 枚举仅 `USER` / `ADMIN` |
| `entity/Episode.java` | drama(ManyToOne)/episodeNo/title/videoUrl/durationSec |
| `entity/Favorite.java` | user+drama，(user_id, drama_id) 唯一约束（幂等） |
| `entity/UserLike.java` | user+drama，同上，表名避保留字 like |
| `entity/WatchProgress.java` | user+drama+episode+progressSec，唯一约束——**但无 Repository、无任何调用方，纯定义未接线** |

### Controller 实际暴露的 HTTP 端点（全部）
`controller/AuthController.java`：
- `POST /api/auth/register`（注册即登录，重名 409）
- `POST /api/auth/login`（失败统一 400）
- `GET /api/auth/me`（需登录）

`controller/DramaController.java`：
- `GET /api/dramas`（ONLINE 按点赞降序，@EntityGraph 防 N+1）
- `GET /api/dramas/{id}`（可选登录，返回 liked/favorited；OFFLINE 一律 404）
- `GET /api/dramas/{id}/episodes`

`controller/PingController.java`：
- `GET /api/ping`

**注意：没有任何 POST like/favorite、/api/me/**、/api/admin/** 的 Controller 方法。** SecurityConfig 里的对应鉴权规则是"规则先行"（注释明确写"接口里程碑 4/5 实现"），调用会 404/405。

### Service / Repository
- `service/AuthService.java`（register/login/getMe）、`service/DramaService.java`（listOnline/getDetail/listEpisodes）。仅此两个。
- `repository/`：UserRepository(findByUsername)、DramaRepository(findByStatusOrderByLikeCountDesc)、EpisodeRepository(findByDramaIdOrderByEpisodeNoAsc)、FavoriteRepository 与 UserLikeRepository（各只有 `existsByUserIdAndDramaId`——只读，无保存方法）。**无 WatchProgressRepository。**

### security/ JWT 链路
- `security/JwtService.java`：jjwt 0.12.6 HS256，载荷 sub=userId+username+role，过期 `app.jwt.expire-hours`（72h）。
- `security/JwtAuthFilter.java`：Bearer 解析入 SecurityContext；无 token/坏 token 一律放行保持匿名，由授权规则兜底 401。非 Spring bean，SecurityConfig 显式构造防双注册。
- `security/AuthUser.java`：UserDetails，`@AuthenticationPrincipal` 用；`security/UserDetailsServiceImpl.java`：桥接件（登录实际走 AuthService 直查，此类供里程碑 5 复用，devlog 偏差记录 #4 承认是"预备件"）。
- `config/SecurityConfig.java` 鉴权规则（顺序即优先级）：
  - `GET /api/auth/me` authenticated → `/api/auth/**`、`/api/ping`、`/uploads/**` permitAll
  - `GET /api/dramas`、`/api/dramas/{id}`、`/api/dramas/{id}/episodes` permitAll
  - `POST /api/dramas/*/like`、`/api/dramas/*/favorite` authenticated（**规则已配，接口不存在**）
  - `/api/me/**` authenticated（接口不存在）
  - `/api/admin/**` hasRole("ADMIN")（接口不存在）
  - anyRequest authenticated；STATELESS、关 CSRF、401 统一 JSON `{code:401,...}`
- `config/WebMvcConfig.java`：`/uploads/**` → 本地磁盘 `app.upload-dir` 静态映射。

### config/DataSeeder 种子数据
`config/DataSeeder.java`：CommandLineRunner，用户表为空才灌（幂等）。**6 部剧 × 各 4 集 = 24 集**（甜宠/都市/逆袭/古装/悬疑分类，likeCount 定值 963~3258，封面 `/uploads/covers/drama-XX.svg`，视频 `/uploads/videos/drama-XX/ep-XX.mp4`），**2 个账号**：admin/admin123(ADMIN)、demo/demo123(USER)，BCrypt 加密。

### 测试
仅 1 个测试类 `src/test/java/com/shortdrama/api/ShortdramaApiApplicationTests.java`——只有 `contextLoads()` 一个空上下文加载测试。无任何业务/接口测试（devlog 提到的 `scripts/api-test.sh` curl 验收脚本已不存在于 scripts/ 目录）。

## 2. 前端 apps/web（Vite + React + TS + Tailwind v4）

### 路由（`/Users/honlnk/project/short-drama-web/apps/web/src/router/index.tsx`）
| 路径 | 组件 |
|---|---|
| `/` | HomePage（分类筛选条 + 响应式网格 + loading/空态/失败重试） |
| `/drama/:id` | DramaDetailPage |
| `/drama/:id/play` | PlayerPage |
| `/login` | LoginPage（含 demo/admin 快捷填充） |
| `/register` | RegisterPage（确认密码、注册即登录） |
| `*` | 重定向 `/` |

`components/RequireAuth.tsx` 整页登录守卫已写好但**无路由挂载**（注释：备给里程碑 4"我的收藏"）。

### api/client.ts
全站唯一 axios 实例：请求拦截器从 localStorage 注入 `Bearer token`；响应拦截器遇 401 清 token 跳 `/login?from=`（`skipAuthRedirect` 配置豁免启动态校验）；`getApiErrorMessage` 从 `{code,message,data}` 包体提取可读信息。

### AuthContext（`store/AuthContext.tsx`）
提供 `user/token/ready/login/logout`；挂载时用存储 token 调 `getMe()` 恢复登录态，失败静默降级访客。`components/Layout.tsx` 顶栏展示昵称/退出/登录注册入口。

### 播放页 PlayerPage 交互现状
- **选集**：真实——URL `?ep=` 定位当前集（非法回落第 1 集），选集按钮 setSearchParams，video `key={current.id}` 强制重载，`autoPlay`（`pages/PlayerPage.tsx:41-46,76-83`）。
- **播放进度续播**：**完全没有**——不监听 timeupdate、不写 localStorage、无任何 WatchProgress 相关前端代码。
- **点赞/收藏按钮**：**纯 UI**——`hooks/useDramaInteractions.ts:6` 注释自述"仅 UI 状态回显 + 未登录引导登录，不写库（写库属里程碑 4）"；点击已登录只弹"功能即将上线，敬请期待"提示。`detail.liked/favorited` 状态来自后端 exists 查询，但永远无入口变 true（无写接口）。

## 3. 管理后台 apps/admin
**只有骨架**。`/Users/honlnk/project/short-drama-web/apps/admin/src/App.tsx` 是单文件占位页："阶段0 脚手架占位页"，唯一功能是 fetch `/api/ping` 显示连通状态 Tag。无路由、无登录、无任何业务页面（后端 `/api/admin/**` 接口同样为零）。

## 4. scripts/ 与根目录
| 文件 | 作用 |
|---|---|
| `scripts/dev.sh` | 一键起三服务：api(8080, mvnw spring-boot:run) + web(3000) + admin(3001)，日志入 logs/{api,web,admin}.log，Ctrl-C 全杀 |
| `scripts/fetch-seed-media.sh` | 从 Mixkit 幂等下载竖屏 9:16 素材到 seed-assets/videos/（默认 50 个，.fail 清单） |
| `scripts/allocate-seed-media.sh` | 从 seed-assets 按分类选片落位 apps/api/uploads/：24 个 mp4（drama-01..06/ep-01..04）+ 6 张渐变 SVG 封面，输出校验报告，幂等 |
| `scripts/mixkit-ids.txt` / `mixkit-titles.txt` | 243 个候选 ID / 366 条 ID→标题映射，供选片 |

根目录另有 `README.md`（架构图/快速开始/里程碑进度表）、`AGENTS.md`、`docs/`（REQUIREMENTS/PRD/ARCHITECTURE/devlog/plan 三阶段计划）、`logs/`、`seed-assets/`（已 gitignore）。

## 5. docs/devlog.md 最近日志（共 128 行）
- **2026-10-16 15:45** 文档归档：REQUIREMENTS/PRD/ARCHITECTURE 移入 docs/，新增根 README，MySQL 凭据定稿 root/root123 占位符写法。
- **15:55** 用户拍板：目录/端口统一 apps/api(8080)+web(3000)+admin(3001)，demo 账号定稿 demo/demo123，docs/PLAN.md 退役（计划唯一存放处 docs/plan/）。
- **16:05** 种子素材就绪：Mixkit 竖屏方案，50 视频入 seed-assets，交付 fetch 脚本与 ID 清单；古装分类无实拍素材用氛围素材顶替。
- **19:30 阶段1 门禁通过**：6 表实体 + DataSeeder（6剧×4集+2账号）+ GET /api/dramas + allocate 脚本 + C 端首页真实渲染；偏差记录（status 取 ONLINE/OFFLINE 而非 PUBLISHED、媒体重建拆两步、幽灵 users 表插曲等）。
- **16:40 阶段2 门禁通过（最新一条）**：JWT 鉴权链路 + 注册/登录/me + 详情/剧集接口 + C 端五页面路由与登录态；curl 与浏览器全路径验收全绿；偏差记录（播放页一次取齐数据、登录失败用 400 防拦截器劫持、手机档保留顶栏等）。**"下一步"明确写着：里程碑 4 点赞/收藏写库 → 里程碑 5 管理后台。**

## 6. TODO/FIXME/占位标记
- `apps/admin/src/App.tsx:22` — "阶段0 脚手架占位页"（唯一的真占位）
- `apps/web/src/hooks/useDramaInteractions.ts:6` — "不写库（写库属里程碑 4）"、"功能即将上线"提示文案
- `apps/web/src/components/RequireAuth.tsx` — 注释"备好供里程碑 4…暂无路由挂载"
- `apps/api/.../SecurityConfig.java:24-25` — 注释"/api/me/**（接口里程碑 4 实现，规则先行）"、"/api/admin/**（里程碑 5 实现，规则先行）"
- 其余命中均为表单输入框的 HTML placeholder 属性，非代码占位。

## 简明结论

| 需求 | 状态 |
|---|---|
| 注册/登录（JWT 会话保持、401 守卫、登录态恢复） | **已真实打通**（前后端 + 鉴权链路完整） |
| 短剧展示（首页推荐流/详情/竖屏播放/选集/分类） | **已真实打通**（真实 MySQL 数据 + 本地媒体，无写死数据） |
| 点赞 | **未落地**——仅 UI：按钮有状态回显样式与登录守卫，无 POST 接口、无写库、仓储只有 exists 查询 |
| 收藏 / 我的收藏 | **未落地**——同上；"我的收藏"页面前后端都不存在 |
| 播放进度续播 | **完全缺失**——WatchProgress 实体是孤儿（无 Repository 无调用），前端无 timeupdate/续播逻辑 |
| 管理后台 | **仅骨架**——admin 是 ping 连通性占位页，后端 `/api/admin/**` 零接口（鉴权规则先行） |

总体：项目处于"里程碑 2+3 完成门禁"状态（devlog 与 README 进度表口径一致），C 端浏览与用户链路是真实可演示的；互动写库（里程碑 4）与管理后台（里程碑 5）是规划中的下一步，当前只有预埋的实体/鉴权规则/UI 骨架。

---
