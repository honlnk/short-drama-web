# 施工日志（devlog）

格式：日期 + 阶段 + 验收结论 + 偏差记录。每阶段门禁时更新。

## 2026-10-16 · 阶段0 脚手架（进行中）

- 环境核实：Java 21.0.9 (Temurin) / Maven 3.9.11 / Node v24.11.0 / pnpm 10.21.0 / git 2.51.2；start.spring.io 与 npm registry 均可达。
- `git init -b main` 完成；计划定稿写入 `docs/PLAN.md`，含与 PRD 的冲突裁定（目录端口按 apps/* + 3000/3001；演示账号 demo 按 demo123）。
- 待办：apps/api（Spring Initializr）、apps/web、apps/admin（Vite 模板）、scripts/dev.sh、三服务门禁验收。

## 2026-10-16 15:40 · 阶段0 门禁通过（含一次中途技术决策变更）

### 变更与偏差记录

1. **数据库 H2 → 本机 MySQL（用户中途决策，已遵照执行）**：施工期间用户并行修改了 ARCHITECTURE.md / PRD.md / docs/PLAN.md，数据库从「H2 file 模式（零外部依赖）」改为「本机 MySQL 26.7（homebrew，127.0.0.1:3306，非容器）专用库 short_drama，utf8mb4」；用户在会话中提供凭据 root/root123。脚手架随之切换：pom 依赖 h2 → mysql-connector-j、application.yml 数据源改 MySQL、SecurityConfig 移除 h2-console 相关配置。H2 不再是本项目数据库路径；原计划中 3307 docker MySQL 备选方案作废。影响：演示前置条件从「无」变为「本机 mysqld 需在运行」（homebrew services 可设开机自启）。
2. **start.spring.io 已不再提供 3.5.x**（当前默认 4.1.1，仅 4.x 可选）：按 Initializr 生成骨架后手工将 parent 钉到 Maven Central 最新 3.5.16，并把 Boot4 新依赖名（spring-boot-starter-webmvc 等）换回 3.5 命名（spring-boot-starter-web 等）。计划钉 3.5.x 的意图保持不变。
3. **dev.sh 首版即败**：`set -u` 下 `$LOG_DIR` 后紧跟全角「（」，bash 将多字节字符部分字节并入变量名导致 unbound 退出。修复：脚本内变量展开一律 `${}`。已重测通过。
4. **文档冲突裁定**（详见 docs/PLAN.md 执行补记）：目录/端口按计划 apps/* + 3000/3001（PRD 7.2 的 server+5173/5174 为概要性表述）；demo 账号密码按 PRD 用 demo/demo123（计划中的 123456 作废），阶段1 落地。

### 门禁结论（MySQL 模式，全绿）

- `./mvnw package` 构建通过（Java 21 / Boot 3.5.16）。
- `./scripts/dev.sh` 一键启动：api :8080 `/api/ping` 200（日志确认 HikariPool 真实建立 `com.mysql.cj.jdbc.ConnectionImpl` 连接，启动 1.1s）；web :3000、admin :3001 均 200。
- 双前端经 Vite 代理访问 `/api/ping` 均正常返回 pong（前后端链路真实连通，非 mock）。
- 双前端 `tsc -b` 类型检查通过；`short_drama` 库确认为 utf8mb4_unicode_ci。
- 停止验证：dev.sh 退出后 8080/3000/3001 三端口全部释放。

### 下一步

阶段1 后端：实体 → JWT/Security → 服务 → 控制器 → 种子数据（admin/admin123、demo/demo123）→ `scripts/api-test.sh` curl 验收全绿。

## 2026-10-16 15:45 · 文档归档 + README（脚手架会话）

- 根目录归档：REQUIREMENTS.md / PRD.md / ARCHITECTURE.md → docs/（git mv 保留历史）；根目录只留 README.md 与 AGENTS.md（后者供 AI 工具读取，必须固定在根）。
- 引用修复：AGENTS.md 需求来源路径、docs/ARCHITECTURE.md 目录树（顺带把 ASCII 树改为嵌套列表，符合文档表达规范）、docs/plan/第一阶段计划的上游文档相对链接。
- 新增根目录 README.md：功能概览、技术栈、Mermaid 架构图、快速开始、演示账号、数据库说明、文档索引、里程碑进度。
- 发现并行会话新产出 docs/plan/第一阶段-数据底座与种子资源.md（种子数据线），其对脚手架会话的接口约定已对齐：application.yml 凭据改为 `${DB_USERNAME:root}` / `${DB_PASSWORD:root123}` 占位符写法（默认值即本机实际，演示零配置），改后已实测后端启动 + MySQL 连接正常。
- 该第一阶段计划的「待用户拍板 #1（MySQL 凭证）」已由用户在本会话提供 root/root123 并验证可用，记录于此供其会话取用。

## 2026-10-16 15:55 · 用户拍板收口：端口/目录统一、demo 口径定稿、PLAN.md 退役（脚手架会话）

用户明确拍板三项，本会话已落实：

1. **目录/端口**：全仓统一为 apps/api(8080) + apps/web(3000) + apps/admin(3001)。PRD 已同步改写：5.2 节「如 5174」→「3001」，7.2 表 server/web/admin+5173/5174 → apps/*+3000/3001。此前的「PRD 与计划冲突」就此消解，PRD 现与实际一致。
2. **demo 账号**：定稿 `demo / demo123`（admin/admin123 不变）。与 docs/plan/第一阶段 文档口径一致，无代码需改（种子数据未落地）。
3. **docs/PLAN.md 退役**：用户删除该文件，现行计划唯一存放处为 docs/plan/ 目录。README 文档索引、ARCHITECTURE.md 目录结构、AGENTS.md 的相关引用已同步清理。

注：工作树中 .gitignore（seed-assets/ 忽略）与 scripts/fetch-seed-media.sh、scripts/mixkit-*.txt 为第一阶段会话的新增施工物，本会话未纳入提交，由其自行收口。

## 2026-10-16 16:05 · 种子素材就绪（第一阶段·本会话）

- 素材拍板落地：Mixkit 竖屏真人素材方案（替代原横屏测试视频占位方案）。
- 下载完成：50 个视频入 `seed-assets/videos/`（54MB，50/50 成功零失败；单文件 470K~2.1M、时长 8~28 秒；抽查 4 个浏览器实测均 360×640 竖屏）。
- 交付物：`scripts/fetch-seed-media.sh`（幂等下载，带失败清单）、`scripts/mixkit-ids.txt`（243 个候选 ID，前 50 个即已下载集）、`scripts/mixkit-titles.txt`（366 条 ID→标题映射，供按分类选片）、`seed-assets/README.md`（50 个素材清单 + 五分类匹配建议 + License 提示，随素材不入 git）。
- git：`seed-assets/` 已加入 `.gitignore`（`git check-ignore` 验证生效）。
- 计划同步：`docs/plan/第一阶段-数据底座与种子资源.md` 任务 2 已由「写下载脚本」改写为「素材已就绪，仅剩按分类选片复制到 uploads/」；第九节素材拍板项标记已定稿。
- 已知缺口：素材库中无直接对应「古装」分类的内容，施工时用氛围素材顶替并在演示话术说明（详见 seed-assets/README.md 分类说明）。
- 衔接脚手架：本会话拍板 #1（MySQL 凭证）已由脚手架会话记录为 root/root123 并验证可用，本线任务 1（建库 short_drama）已由脚手架门禁确认 utf8mb4_unicode_ci 就绪，不再重复执行。

## 2026-10-16 19:30 · 阶段1 门禁通过：数据底座与种子资源全线落地（第一阶段·本会话）

验收标准一句话达成：**C 端首页用后端真实接口渲染出 6 部短剧封面网格，数据来自 MySQL，视频与封面全部本地资源。**

### 交付物

- 实体 6 张表：`User`（表名 `` `user` `` 避保留字）、`Drama`（status ONLINE/OFFLINE）、`Episode`、`Favorite`、`UserLike`（各带 user_id+drama_id 唯一约束）、`WatchProgress`（user_id+drama_id 唯一）；仓储 `UserRepository` / `DramaRepository`（@EntityGraph 一次取 episodes）/ `EpisodeRepository`。
- `DataSeeder`（CommandLineRunner，用户表为空才灌库）：6 剧 × 4 集 + admin/admin123（ADMIN）+ demo/demo123（USER），密码 BCrypt。
- 接口 `GET /api/dramas`：上线剧按点赞降序，DTO `DramaSummary` 含集数统计。
- 媒体分配：`scripts/allocate-seed-media.sh`（新增，幂等，已重跑两遍验证）从 seed-assets 按分类选片复制 24 个 mp4 至 `uploads/videos/drama-{01..06}/ep-{01..04}.mp4`，生成 6 张分类配色 SVG 封面 `uploads/covers/drama-{01..06}.svg`，输出校验报告（数量/体积/mdls 时长）。`uploads/` 30 文件共 24MB。
- C 端首页（apps/web/App.tsx 重写）：axios 调 `/api/dramas`，暗色主题响应式网格（2/3/4/6 列），卡片含封面、分类角标、点赞（万格式化）、集数；loading/error 态齐全，无任何写死数据。

### 门禁实证（全绿）

- 空库启动自动建 6 表并灌种子；二次启动日志「数据已存在，跳过种子导入」，行数不变（6/24/2）。
- `./mvnw test` 绿；web `tsc -b` 绿。
- `curl /api/dramas` 返回 6 剧 JSON 字段完整；mp4 直连 200 + Range 206（video/mp4）。
- 浏览器验收：首页 DOM 快照 6 张卡片与种子数据逐项一致；截图视觉核验通过（SVG 封面、角标、点赞、集数齐全无错位）；页面内 `<video>` 实际解码 ep-01：readyState=4、20.5s、360×640，3000 代理与 8080 直连两路均通。
- 幂等复验：`fetch-seed-media.sh` 重跑 50 个全跳过 0 失败。

### 偏差记录

1. **drama.status 取值 ONLINE/OFFLINE，非计划的 PUBLISHED**：上游 PRD §6 数据设计即为 ONLINE/OFFLINE，以 PRD 为准（PRD 是需求唯一来源），计划措辞作废。
2. **媒体重建路径拆成两步**：计划写「uploads 由 fetch-seed-media.sh 一键重建」，实际拆为 fetch（下载 50 素材）+ allocate（选片复制 + 封面生成）两个脚本串行，README 已按两步写。
3. **episode.duration_sec 按素材实际时长（8~28s 整数）填**，非计划的「填 5 或 10」；like_count 用固定值（963~3258）非随机——两者都更真实，不影响验收。
4. **插曲：幽灵 `users` 表**——一次测试运行后 SHOW TABLES 列出 `users` 但访问报不存在（遗留元数据残影），drop 全部表后干净重建，6 张表准确无冗余，未复现。
5. mdls 首次跑 allocation 时 Spotlight 未索引返回 null，重跑即正常（时长数据已在报告中，无需依赖）。

### 下一步

阶段2（里程碑 2）：注册/登录/JWT 贯通 → 阶段3 C 端详情页与播放页 → 阶段4 点赞收藏 → 阶段5 管理后台。

## 2026-10-16 16:40 · 阶段2 门禁通过：用户链路与核心演示路径全线落地（第二阶段·本会话）

按《第二阶段计划》完成里程碑 2+3 合并施工。验收标准一句话达成：**访客打开 C 端首页看到真实短剧网格，注册登录后进入详情页看到点赞收藏状态回显，点集进入竖屏播放页正常播放，三档宽度布局无错位；受保护接口无 token 返回 401。**

### 交付物

**后端（apps/api，第一阶段实体与列表接口未动，只新增）**：

- `security/` 包：`JwtService`（HS256 签发/解析，载荷 sub=userId + username + role，72h）、`JwtAuthFilter`（Bearer 解析入 SecurityContext，非法/过期放行交授权规则兜底；非 bean，由 SecurityConfig 显式构造避免 Boot 全局 Filter 双注册）、`AuthUser`（UserDetails，供 @AuthenticationPrincipal）、`UserDetailsServiceImpl`（桥接 Security 基础设施，登录本身走 AuthService 直查仓储）。
- SecurityConfig 替换全量放行：公开 `/api/auth/**`（`/me` 除外，靠规则顺序）、`/api/ping`、`/uploads/**`、C 端浏览 GET（`/api/dramas` 列表/详情/剧集）；登录才能点赞收藏与 `/api/me/**`；`/api/admin/**` 需 ADMIN（规则先行，接口属里程碑 5）；STATELESS + 关 CSRF；401 统一 JSON `{code:401,message:"未登录或登录已过期",data:null}`。
- 认证接口：`POST /api/auth/register`（重名 409「用户名已被注册」，注册即登录返回 token）、`POST /api/auth/login`（失败统一 400「用户名或密码错误」）、`GET /api/auth/me`（登录态校验回填，nickname 查库）。DTO 四件 + `@NotBlank/@Size` 校验；`exception/` 包：BusinessException + GlobalExceptionHandler 统一 `{code,message,data}` 出口（业务/校验/未知异常）。
- 详情与剧集：`GET /api/dramas/{id}`（可选登录：带 token 返回 liked/favorited 真实状态，匿名 false；OFFLINE/不存在一律 404）、`GET /api/dramas/{id}/episodes`（episodeNo 升序）；新增 `FavoriteRepository`/`UserLikeRepository`（existsByUserIdAndDramaId）与 `EpisodeRepository.findByDramaIdOrderByEpisodeNoAsc`；DTO `DramaDetail`/`EpisodeSummary`。

**C 端（apps/web，从单页改造为带路由与全局登录态的应用）**：

- 基础设施：`api/client.ts`（axios 实例，请求拦截塞 Bearer；401 清 token 跳 `/login?from=`，`skipAuthRedirect` 标记豁免启动校验）、`api/auth.ts`、`api/dramas.ts`、`store/AuthContext.tsx`（挂载经 getMe 恢复登录态，失败静默降级访客）、`router/index.tsx`（五路由 + 兜底回首页）、`components/Layout.tsx`（顶栏含登录态/退出）、`components/RequireAuth.tsx`（整页守卫备好，供里程碑 4）、`hooks/useDramaInteractions.ts`（点赞收藏点击守卫：未登录跳登录页带回跳，已登录提示「即将上线」不写库）。
- 页面：HomePage（分类筛选条 + 2/4/6 列响应式网格 + loading/空态/失败重试）、DramaDetailPage（封面左信息右布局、点赞收藏状态回显、选集网格）、PlayerPage（竖屏 9:16 播放、`?ep=` 定位与切换、video key 重载、PC 双栏布局）、LoginPage（含 demo/admin 快捷填充）、RegisterPage（确认密码 + 注册即登录）。

### 门禁实证（全绿）

- curl：注册 200/重名 409、登录 200/错密码 400、`/me` 带 token 200 / 无 token 401 / 伪造 token 401、`GET /api/dramas` 匿名 200（公开接口不回退）、`POST /api/dramas/1/like` 匿名 401。
- 详情/剧集：无 token liked/favorited=false，带 demo token 正确（false/false）；`/episodes` 4 集升序 videoUrl 正确；mp4 HEAD 200 + Range 206（video/mp4）；不存在的剧 404。
- 浏览器（自动化全路径）：首页 6 剧网格真实渲染 → 点「悬疑」只剩《午夜出租车》→ 进详情 → 未登录点点赞跳 `/login?from=%2Fdrama%2F5` → 快捷填充登录回详情且头部显示「演示用户/退出」→ 已登录点赞出占位提示不跳转 → 点第 2 集 URL `?ep=2` 视频 readyState=4（360×640 竖屏、进度 3.4s 证实在播）→ 刷新仍停第 2 集 → 选集切 3 URL/片源同步 → 注册 browser_demo 即登录 → 刷新登录态保持 → 退出回登录/注册。
- 三档宽度（375/900/1440）：首页网格列数实测 2/4/6，均无横向滚动；播放页视频本体 9:16（object-contain 不裁切），PC 档「播放区+右侧信息栏」双栏；四档截图视觉核验无错位/重叠/截断。
- `./mvnw test` 绿；web `tsc -b` 绿；前端源码无任何外网资源引用（断网演示安全）。

### 偏差记录

1. **播放页数据源用详情接口一次取齐**：计划写「GET /api/dramas/{id}/episodes（+详情取剧名）」两次请求，实际 `DramaDetail` 已内嵌升序 episodes，播放页只调一次详情接口。效果一致少一次请求；episodes 独立接口保留供后续使用（如记忆续播）。
2. **登录失败 HTTP 状态取 400 而非 401**：若用 401 会触发前端 axios 拦截器「清 token + 跳登录页」，在登录页内形成错误提示被劫持；400 携带「用户名或密码错误」message 由登录页直接展示。语义无歧义。
3. **手机档播放页保留全局顶栏**：计划写「手机全屏沉浸」，实际保留 56px 顶栏（提供返回首页与登录态入口），播放区 max-h 自适应不出屏。演示价值（随处可导航/退出）优于纯沉浸，如需纯沉浸里程碑 6 再收。
4. **UserDetailsServiceImpl 本阶段为基础设施预备件**：登录走 AuthService 直查 UserRepository（少一层转换），该类供 Spring Security 全局 AuthenticationManager 装配，里程碑 5 管理后台登录可复用。计划原文即列此类，未删。
5. 验收期间注册的 browser_demo/gateuser 测试账号留在库中（真实注册链路的副产物），不影响演示；如需清理可 `DELETE FROM user WHERE username IN ('gateuser','browser_demo')`。

### 下一步

里程碑 4：点赞/收藏写库（POST /api/dramas/*/like、/favorite 与 /api/me/favorites，规则已配好）→ 里程碑 5 管理后台。

## 2026-10-16 17:50 · 阶段4 门禁通过：体验加分项在独立 worktree 落地（第四阶段·本会话）

按《第四阶段计划》在独立 worktree（`../short-drama-web-m4`，分支 `feat/m4-experience`，基于 main@e9bea5b）并行施工，与第三阶段（main 工作区在途）零文件冲突。验收标准一句话达成：**demo 账号在播放页看片，进度自动上报服务端；重新打开该剧自动从上次集数与进度续播；首页搜索框实时过滤短剧；播放页滚轮/键盘/手势切集与自动连播全部可用。**

### 交付物

**后端（apps/api，进度接口全为新文件；搜索只动 listOnline 一条链路）**：

- `repository/WatchProgressRepository`（findByUserIdAndDramaId / findByUserIdOrderByUpdatedAtDesc）；`dto/ProgressReportRequest`（@PositiveOrZero 校验）、`dto/ProgressReportResponse`、`dto/HistoryItem`（DramaSummary 八字段平铺 + 集信息 + updatedAt）。
- `service/ProgressService`：report 为 upsert（每用户每剧一条），剧 ONLINE/OFFLINE 均可写（与 history 不过滤 OFFLINE 口径一致），episode 归属校验 400、剧不存在 404、progressSec 截断 `[0, durationSec]`、唯一索引冲突捕获后重查收敛；**刻意不开外层事务**——save 的独立事务让冲突干净回滚（与第三阶段 InteractionTxService 拆 bean 解决同一问题，此为「无外层事务」路线，类注释有说明）。history 倒序 + 脏记录（Drama/Episode 被级联删）逐条隔离跳过。
- `controller/ProgressController`（`/api/me` 前缀，与第三阶段 MeController 同前缀不同文件互不包含）：`PUT /api/me/progress?dramaId=`、`GET /api/me/history`。
- 搜索（PL-06）：`DramaRepository` 新增 `findByStatusAndTitleContainingIgnoreCaseOrderByLikeCountDesc`（@EntityGraph episodes），`DramaService.listOnline(keyword)` trim 空串走原查询（无参行为与阶段2 完全一致），`DramaController` 透传 `?keyword=`。
- 单测 `ProgressServiceTest` 8 例：新建/原位更新/上下限截断/跨剧 400/缺剧 404/唯一索引冲突收敛/history 倒序与孤儿跳过。

**C 端（apps/web，只动 HomePage/PlayerPage/dramas.ts + 新增 progress.ts；vite.config 加 VITE_PROXY_TARGET 一行）**：

- `api/progress.ts`：reportProgress（静默失败）/getHistory。
- HomePage：搜索框（受控 + 300ms 防抖、清空按钮、空结果文案区分「没有找到相关短剧」/「该分类暂无短剧」、keyword 后端 × category 前端的叠加过滤）。
- PlayerPage：进度上报（timeupdate 5s 节流 + pause/visibilitychange(hidden)/切集前/卸载补报，未登录零调用）；记忆续播（无 `?ep=` 时按 history 恢复集数与秒数，`?ep=` 显式存在时 URL 优先，loadedmetadata 后设置 currentTime，距片尾 <3s 视为已看完从头播，提示「已为你续播到第 X 集」3s 淡出）；滚轮（节流 500ms）/键盘 ↓→↑←/触摸纵向滑动 >50px 切集，首尾集边界提示「已经是第一/最后一集」；自动连播（ended 非末集自动进下一集并显式 play()，被浏览器拦则叠「▶ 继续下一集」按钮；末集停留并提示）。

### 门禁实证（全绿）

- 并存：主工作区 8080/3000 与 worktree 8081/3002 四服务同时在线互不干扰；3002 代理经 VITE_PROXY_TARGET 指向 8081 转发验证。
- 进度 curl：无 token 两接口 401；上报剧1第2集 35s（时长 13s）→ `{updated:true}` 库中截断 13；同剧换第3集 8s → 仍一条记录且 episode/秒数已更新（upsert）；9999 → 截断 20；跨剧 episodeId 400；剧不存在 404；`GET /api/me/history` 倒序（最新观看在前）且字段齐全。
- 搜索 curl：`?keyword=豪门` 只含《闪婚老公是豪门》；`?keyword=zzzz` 空数组 200；无 keyword 全量 6 剧（回归）；前后空格 trim 命中。
- 浏览器（自动化，登录态 token 注入等价登录）：搜索「豪门」防抖后剩 1 部 → 清空按钮恢复 6 部 → 选「甜宠」（2 部）再搜「豪门」叠加剩 1 部；播放页自然播完第 1 集自动连播 1→2→3→4 全程自动推进且逐集上报（库记录随播更新）；末集 ended 后 URL 停留 `?ep=4`；固定库值 ep2/5s 重进 → 恢复 ep=2 从 5s 起播且提示正确；库值 12s/13s 贴片尾重进 → 从头播（tail-grace）；显式 `?ep=3` → 从头播不恢复（URL 优先）；键盘 ↑/← 逐集回退且 URL 同步；末集 ↓ 弹「已经是最后一集」、首集 ↑ 弹「已经是第一集」；滚轮 deltaY>0 下一集；模拟触摸上滑/下滑 100px 正反向切集；未登录播 6.5s+ 无任何 `/api/me/progress` 请求且无续播提示（XHR 钩子实证零调用）。
- `./mvnw test` 9 例全绿（含新 8 例）；web `pnpm exec tsc -b` 绿。
- 阶段2 回归：无 keyword 列表全量、详情/播放路由、点赞收藏按钮与 hint 均未动（详情页与 DramaDetailPage 零改动）。

### 偏差记录

1. **计划门禁示例词「甜」不可用**：计划任务 2 门禁写「输入『甜』只剩甜宠分类两部剧」，但「甜」不在任何剧名中（甜宠是分类名），按拍死的 title LIKE 实现搜索「甜」为空结果——空结果提示正好借此验证。改用「豪门」（唯一命中）完成同一门禁语义。计划文档措辞缺陷，非实现偏差。
2. **修复 WebKit `type="search"` 原生清除按钮与自定义清空按钮重叠**：搜索框首版 `type="search"`，WebKit 给其渲染原生 × 与自定义清空按钮叠放导致点击不可达。改 `type="text"`（保留自定义按钮与 aria-label）。
3. **uploads 媒体目录计划未提**：uploads 不入 git，worktree 内仅有 .gitkeep，8081 静态服务会 404。以 symlink 指向主工作区 `apps/api/uploads` 共享（只读共享无冲突）。此链接不入 git，合并回 main 后自然失效（main 工作区自身有真实目录）。
4. **进度接口返回裸 DTO 而非 ApiResponse 包装**：与 C 端既有接口（/api/dramas、/api/auth/*）一致；第三阶段互动接口用 ApiResponse 是其在途选择，两风格在错误出口上已由 GlobalExceptionHandler 统一。
5. 续播浏览器验证期间出现「后台页持续播放并上报导致库值自干扰」——为测试方法论问题（固定库值后 goto 会触发卸载补报覆盖），非产品缺陷；改用页面内 seek 设值后验证通过。

### 下一步

等第三阶段收口上 main 后合并本分支（预期冲突面：DramaService/DramaController 签名行，保留双方即可）；PL-03 已看标记与 PL-05 观看历史前端页留第五阶段（挂 /mine 路由）；里程碑 7 联调彩排。
