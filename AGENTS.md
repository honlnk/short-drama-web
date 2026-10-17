# AGENTS.md — short-drama-web

## 项目背景

AI Coding 现场实战题：开发一个类似「红果短剧」的简化版短剧 Web APP。需求唯一来源是 `docs/REQUIREMENTS.md`，动工前必读。这是一道限时现场题，最终需要现场演示运行并讲解设计思路。

## 硬性技术约束（题目规定，不可替换）

- 后端必须使用 **Java**
- 前端必须使用 **React**（React Native / Flutter 仅为可选加分项，默认不做）
- 必含功能：用户注册/登录、短剧内容展示、点赞、收藏、管理后台（短剧内容增删改查）
- 前后端及数据库必须**真实打通**，禁止用 mock 数据糊弄演示路径
- 项目必须可在本地正常启动并现场演示

## 当前状态（阶段4 体验加分项已并入 main，2026-10-17）

三工程 monorepo 就绪（阶段0）+ 数据底座（阶段1）+ 用户链路与核心演示路径（阶段2）+ 互动闭环与管理后台（阶段3）+ 体验加分项（阶段4，worktree 并行施工后合并，验收清单见 `docs/plan/第四阶段-体验加分项并行计划.md` 第六节，全部通过）。题目必含功能全部落地，里程碑 6 已完成 PL-01/02/04/05 后端/06，剩余 PL-03 已看标记、PL-05 历史前端页（等下一阶段）与联调彩排。当前能力：

- 统一响应包体 `{code,message,data}`（成功 code=0；错误经 GlobalExceptionHandler，401/403 也是 JSON）；JWT HS384 72h（密钥 416bit 由 jjwt 按位宽自动映射）；前端 `client.ts` 响应拦截器统一拆包。
- 互动写库：`POST /api/dramas/{id}/like|favorite` 幂等切换（乐观更新+响应校准+失败回滚），`GET /api/me/favorites` 倒序收藏列表；C 端新增 `/mine`（我的）与 `/mine/favorites`（我的收藏，RequireAuth 守卫）。
- 管理后台（`/api/admin/**` 需 ADMIN，无 token 401 / USER 403）：短剧分页列表（keyword 检索）/新增/编辑/上下架/级联删除（watch_progress→favorite→user_like→drama），剧集 CRUD（`(drama_id, episode_no)` 唯一约束），`POST /api/admin/files/upload` 文件上传（扩展名白名单+大小上限+magic bytes 三重校验，落 `uploads/files/<uuid>.<ext>`）。
- admin 前端（:3001，antd 6）：登录页预填 admin/admin123（非 ADMIN 拒入）、短剧列表（封面/状态 Tag/上下架/删除二次确认/分页）、短剧表单（封面上传）、剧集管理（视频上传+时长元数据自动读取）。
- 观看进度与续播（阶段4）：`PUT /api/me/progress?dramaId=` 每用户每剧 upsert（截断/归属校验/唯一索引冲突收敛）+ `GET /api/me/history` 倒序；播放页 5s 节流上报 + 暂停/隐藏/切集/卸载补报，重进自动续播（`?ep=` 显式时 URL 优先，距片尾 <3s 从头播）。
- 播放增强（阶段4）：滚轮/键盘（↓→↑←）/触摸滑动切集 + 首尾集边界提示；ended 自动连播（被浏览器拦时叠「继续下一集」按钮，末集停留提示）。
- C 端搜索（阶段4）：`GET /api/dramas?keyword=` 标题模糊（忽略大小写），首页搜索框 300ms 防抖即输即搜、清空按钮、空结果文案区分、与分类筛选叠加。
- 后端单测 35 个全绿（阶段3 27 + 阶段4 8）；断网演示项已验证（C 端首页/播放页/后台列表资源审计全部本机来源，源码无外链）。

施工日志见 `docs/devlog.md`。

### 构建 / 启动 / 测试命令

| 操作 | 命令 |
|---|---|
| 一键启动三服务 | `./scripts/dev.sh`（日志写 `logs/`，Ctrl-C 全部停止） |
| 后端单独启动 | `cd apps/api && ./mvnw spring-boot:run`（:8080） |
| 用户端 web | `cd apps/web && pnpm dev`（:3000） |
| 管理后台 admin | `cd apps/admin && pnpm dev`（:3001） |
| 后端构建 / 测试 | `cd apps/api && ./mvnw package` / `./mvnw test` |
| 前端类型检查 | `cd apps/web && pnpm exec tsc -b`（admin 同） |
| 重建种子媒体 | `./scripts/fetch-seed-media.sh 50 && ./scripts/allocate-seed-media.sh` |

### 目录结构与分层规则

- `apps/api`：Spring Boot 3.5.16（Java 21、Maven mvnw）。包 `com.shortdrama.api` 分层：`config/`（SecurityConfig JWT 无状态鉴权 + 401/403 JSON、WebMvcConfig 映射 `/uploads/**`、DataSeeder 种子灌库）、`security/`（JwtService/JwtAuthFilter/AuthUser/UserDetailsServiceImpl）、`entity/`（User/Drama/Episode/Favorite/UserLike/WatchProgress，drama.status 取 ONLINE/OFFLINE）、`repository/`、`service/`（DramaService/AuthService/InteractionService+InteractionTxService 编排与事务分离/AdminDramaService/AdminEpisodeService）、`storage/`（StorageService 接口 + LocalStorageService）、`controller/`（`GET /api/ping`、`GET /api/dramas`、`GET /api/dramas/{id}`、`GET /api/dramas/{id}/episodes`、`POST /api/dramas/{id}/like|favorite`、`POST /api/auth/register|login`、`GET /api/auth/me`、`GET /api/me/favorites`、`/api/admin/dramas`+`episodes`+`files/upload` 全套）、`dto/`（含 ApiResponse 统一包体）、`exception/`（BusinessException + GlobalExceptionHandler）。鉴权规则：点赞/收藏/`/api/me/**` 需登录，`/api/admin/**` 需 ADMIN。
- `apps/web`：C 端，Vite + React + TS + Tailwind v4 + react-router v7 + axios；路由 `/`（首页分类筛选+网格）、`/drama/:id`（详情）、`/drama/:id/play?ep=`（竖屏播放）、`/login`、`/register`、`/mine`、`/mine/favorites`；`api/client.ts` 统一带 token、解包体（code≠0 即 reject）与 401 跳转，`store/AuthContext.tsx` 全局登录态；`/api`、`/uploads` 经 Vite 代理转发 8080。
- `apps/admin`：管理后台，同栈 + Ant Design 6（仅 PC）；`api/client.ts` 用独立 `admin_token` localStorage 键（与 C 端 `token` 隔离）；路由 `/login`、`/`（列表）、`/dramas/new`、`/dramas/:id/edit`、`/dramas/:id/episodes`，RequireAdmin 守卫（非 ADMIN 登出拒入）；代理同上。
- `apps/api/uploads/`：封面/视频本地存储目录（`/uploads/**` 静态服务）：`covers/`+`videos/` 为种子媒体（scripts 两个脚本重建）、`files/` 为后台运行期上传产物；均不入 git。
- `docs/`：plan/（分阶段实施计划，现行计划唯一存放处）、devlog.md（施工日志）。

### 数据库连接与初始化

- 本机 MySQL 26.7（homebrew 原生安装，`127.0.0.1:3306`，**非容器**；docker 里的 linkseek-mysql 与本项目无关），专用库 `short_drama`（utf8mb4_unicode_ci，已建好）。
- 凭据 `root / root123`，配置在 `apps/api/src/main/resources/application.yml`，写法 `${DB_USERNAME:root}` / `${DB_PASSWORD:root123}`（默认值即本机实际，支持环境变量覆盖）。
- 建表：JPA `ddl-auto=update` 启动自动建表；种子：DataSeeder 以用户表为空为触发，空库首启自动导入（6 剧/24 集/2 账号），二次启动跳过。无需手工执行任何 SQL。
- 演示账号：`admin / admin123`（ADMIN）、`demo / demo123`（USER），BCrypt 加密随种子灌入。

## 工作约定

- 演示优先：所有改动必须保证 `启动后端 → 启动前端 → 现场演示` 这条路径始终可用，破坏可运行性的改动优先修复
- 面向评委演示的关键页面（短剧列表/播放、登录注册、管理后台）保持整洁可用
- 初始账号、种子数据（至少几部可播放的短剧）要随项目提供，避免演示时现造数据
