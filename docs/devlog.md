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

- `security/` 包：`JwtService`（HS384 签发/解析——52 字符密钥 416bit，jjwt `signWith(key)` 按位宽自动选最强算法；原口径写 HS256 系笔误，阶段3 review 修正，载荷 sub=userId + username + role，72h）、`JwtAuthFilter`（Bearer 解析入 SecurityContext，非法/过期放行交授权规则兜底；非 bean，由 SecurityConfig 显式构造避免 Boot 全局 Filter 双注册）、`AuthUser`（UserDetails，供 @AuthenticationPrincipal）、`UserDetailsServiceImpl`（桥接 Security 基础设施，登录本身走 AuthService 直查仓储）。
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

## 2026-10-16 · 阶段3 任务1 施工中 · review 插单修复两项（会话中断处理）

阶段3 任务1（互动接口）代码落地后、门禁验收前，用户 review 提出两个问题，逐一核实并修复：

### 1. 统一响应包体未落地（🔴，成立，已修实现）

- **核实**：PRD 接口规范明文「统一响应结构 `{ code, message, data }`」；阶段2 实际只落地了错误侧（GlobalExceptionHandler）与 ping，成功响应（auth/dramas）为裸数据——阶段2 门禁当时未覆盖成功包体形态，漏检。
- **修复**（以 PRD 为准修实现，不改 PRD 迁就现实）：
  - 后端：`ApiResponse` 增加 `ok(data)` 工厂（code=0, message="ok"）；全部成功返回包体——PingController（保留 message="pong"）、AuthController ×3、DramaController ×3、以及本阶段新写的 InteractionController ×2、MeController ×1。
  - 前端 web：`client.ts` 响应拦截器统一拆包（识别 `{code,message,data}` → code=0 时 `response.data = body.data`，页面层 `res.data` 消费形状不变；code≠0 且 HTTP 200 的业务错误转 reject 走既有错误提示链）。
  - 阶段3 计划 §三「衔接表」相应行已加修正注记。
- **影响面**：属对阶段2 已交付接口契约的修订（计划原文「不改第一、二阶段契约」，此为 review 授权的例外）；admin（任务 4）后续按同口径实现。

### 2. JWT 签名算法实为 HS384 非文档口径 HS256（成立，修文档不修代码）

- **核实**：`JwtService.signWith(key)` 未显式指定算法，jjwt 按密钥位宽自动选最强——52 字符密钥=416bit → HS384（token header `{"alg":"HS384"}` 实证）。强度高于 HS256，无安全或兼容问题（解析侧 verifyWith 对称校验，不校验 alg 白名单）。
- **处置**：代码行为不动（更强无理由降级），修正全部文档/注释口径共 6 处：AGENTS.md「当前状态」、devlog 阶段2 条目（就地修正并注明）、第二阶段计划 §四/§五、application.yml 注释、JwtService javadoc（补充自动映射机制说明，防未来换密钥时再漂移）。

### 其他

- 中断前回归另发现两处待办（属阶段3 范围，尚未处理）：USER 调 `/api/admin/**` 返回 401 而非计划门禁的 403（SecurityConfig 缺 accessDeniedHandler 配置，任务 3 修）；未匹配路由异常被 GlobalExceptionHandler 兜成 500 而非 404（计划外问题，仅记录不处理）。

## 2026-10-16 17:10 · 阶段3 任务1 门禁通过：点赞/收藏写库 + 我的收藏接口

按《第三阶段计划》任务 1 完成互动后端。Controller 选型记录（计划允许自选）：独立 `InteractionController` + `InteractionService`（编排，不开事务）+ `InteractionTxService`（写库事务边界，独立 bean）——不并入 DramaController，保持 C 端只读链路纯净。

**交付物**：`POST /api/dramas/{id}/like` / `favorite`（幂等切换）、`GET /api/me/favorites`（收藏时间倒序 DramaSummary）；仓储补 `deleteByUserIdAndDramaId`（返回行数防并发重复扣减）、`findByUserIdOrderByCreatedAtDesc`（EntityGraph 带 drama+episodes）；`DramaSummary.from` 工厂统一映射（DramaService 私有映射收敛至此）；单测 12 个（InteractionServiceTest 7 + InteractionTxServiceTest 4 + 既有 contextLoads）。

**门禁实证（curl 全绿）**：
- 匿名调 like/favorite/favorites → 401 包体；demo token 详情回显与操作一致（liked 随切换变、favorited 随收藏变）。
- 点赞往返：2461 → 2462 → 2461；并发双击（两请求同发）后查库 user_like 仅 1 行、计数只 +1——唯一索引兜底生效。
- 收藏剧 1+剧 2 → /me/favorites 返回 2 条且倒序正确（剧 2 在前）；再取消后清零。
- 门禁数据已通过接口反向清理（库回种子态：0 like / 0 fav，剧 2 计数回 1987）。
- `./mvnw test` 全绿。

**偏差记录**：
1. **唯一索引冲突兜底的实现结构**：计划原文「捕获 DataIntegrityViolationException 按已存在处理」若在同一 @Transactional 内 try-catch，Spring 会因异常穿越仓储代理把共享事务标记 rollback-only，提交时反抛 500。实际改为：编排层不开事务，写入方法（addLike 等）在独立 bean 中各自成事务，冲突时该事务干净回滚、编排层捕获后新事务重读最终状态返回——语义与计划一致（不抛 500、按最终状态返回）。
2. 取消点赞按 `deleteByUserIdAndDramaId` 实际删除行数决定是否回减计数（计划只说计数-1 下限 0）：并发重复取消时第二次删 0 行不再减，杜绝计数漂移；下限 0 仍保留双保险。

## 2026-10-16 17:40 · 阶段3 任务2 门禁通过：前端互动闭环全线接通

**交付物（apps/web）**：`api/dramas.ts` 增 toggleLike/toggleFavorite；`api/me.ts`（listMyFavorites）；`useDramaInteractions` 重写为真实写库（乐观更新 + 响应校准 + 失败回滚提示，未登录跳 `/login?from=`）；DramaDetailPage/PlayerPage 接入新 hook；新页面 MinePage（用户信息 + 收藏入口 + 退出）与 FavoritesPage（复用首页卡片网格 + 空态）；路由 `/mine`、`/mine/favorites` 挂 RequireAuth 守卫；Layout 顶栏加「我的」（≥md）+ 手机档底部 Tab 首页/我的（PRD 5.3），内容区加 pb-14 防遮挡。「功能即将上线」占位文案随旧 hook 重写删除，无残留。

**门禁实证（浏览器自动化全绿）**：
- 未登录点收藏 → `/login?from=%2Fdrama%2F1` → demo 快捷填充登录 → 自动回跳详情页且顶栏显示「演示用户」。
- 已登录点赞：按钮 ♥ 点赞→♥ 已赞、计数 2461→2462 即时变化；收藏同构；刷新页面状态与计数保持。
- `/mine` 见用户卡（演/@demo/用户标签）→「我的收藏」→ 收藏卡片（点赞数 2,462 与库同步）→ 回详情取消收藏 → 收藏页空态「还没有收藏，去首页逛逛」；取消点赞后计数回 2,461（种子态还原，查库 0 like/0 fav）。
- 未登录点赞 → 跳登录页（引导态正确）。
- `pnpm exec tsc -b` 绿。

**偏差记录**：
1. **MinePage 退出登录改硬跳转**：计划「退出 → 跳首页」原用 `navigate('/')`，实测在 RequireAuth 包裹下与「user 置空触发的守卫重定向」竞态，被带去 `/login?from=/mine`。改 `window.location.assign('/')`（与 client.ts 401 处理同款先例），重验退出后落 `/`。
2. 浏览器自动化侧注（非项目缺陷）：本会话 IAB 的 Playwright locator.click 对部分页面元素动作性超时（元素可见、elementFromPoint 命中自身），改用坐标点击 / evaluate 派发真实 DOM click 完成 gate，不影响真实用户。

## 2026-10-16 17:50 · 阶段3 任务3 门禁通过：管理接口 + StorageService + 上传

**交付物（apps/api）**：`storage/` 包（StorageService 接口 + LocalStorageService：uuid 落盘 `uploads/files/`、扩展名白名单 + 大小上限 + magic bytes 文件头嗅探三重校验、delete 防路径穿越）；multipart 50MB 显式配置；`POST /api/admin/files/upload`；`AdminDramaController`（分页列表/新增/编辑/级联删除）+ `AdminEpisodeController`（列表/新增/编辑/删除）+ DTO 五件；Episode 实体补 `(drama_id, episode_no)` 唯一约束（ddl-auto 重启自动加索引）；WatchProgressRepository（仅级联清理用）；SecurityConfig 补 `accessDeniedHandler`（403 JSON，修复前置回归发现的「USER 调后台接口返回 401」）；GlobalExceptionHandler 补 MaxUploadSize/DataIntegrity 兜底。单测 27 个全绿（新增 LocalStorageServiceTest 6 + AdminDramaServiceTest 3 + AdminEpisodeServiceTest 6）。

**门禁实证（curl 全绿）**：
- demo(USER) 调 `/api/admin/**` → 403「无权限访问该功能」；无 token → 401。
- 封面上传返回 `/uploads/files/<uuid>.jpg`，8080 直连 200（image/jpeg）与 3000 代理 200 双路可访问；51MB 假 mp4 → 400 明确文案；`.txt` 直传与「txt 内容伪装 .mp4 后缀」均 400（magic bytes 拦截）。
- 新增短剧（用刚上传封面）→ 后台列表 total=7 且 keyword 检索命中、C 端列表出现；加两集（上传真实 mp4）→ C 端详情 2 集可播源；episodeNo 重复 → 400「集数已存在」。
- 编辑剧名 → C 端详情实时变；下架 → C 端详情 404 + 列表消失 + 后台仍可见；再上架恢复 200。
- 删除该剧 → 后台消失，查库 episodes/likes/favorites/progress/drama 计数全部归零，`uploads/files/` 下三个文件仍在（决策：仅解引用不删磁盘）；C 端 404。

**偏差记录**：
1. 分页返回结构：计划写 `Page<AdminDramaRow>`，实际自定义 `PageResponse<T>`（content/page/size/total）——Spring Data Page 直接序列化结构不稳定（含 unfetched 内容告警），收敛 DTO 对前端更友好。
2. 删除级联顺序：watch_progress → favorite → user_like → drama（cascade 级联 episodes）。计划未提 FK 顺序，实际 watch_progress 引用 episode 的外键要求先清进度再删剧，否则约束炸。
3. 删集（AdminEpisodeService.delete）先清引用该集的 watch_progress——计划只写了剧级级联，集级删除同样有 FK 依赖，补齐。
4. 封面/视频 URL 校验范围：计划只对封面要求 `/uploads/` 开头，视频 URL 同口径收紧（防外链破坏断网演示）。

## 2026-10-16 19:25 · 阶段3 任务4 门禁通过：管理后台前端全链路

**交付物（apps/admin）**：`api/client.ts`（独立 `admin_token` 键、包体解包、401 跳登录）+ `api/admin.ts`（后台全套类型化 API）；`AdminAuthContext` + `RequireAdmin` 守卫（无 token 跳登录、role≠ADMIN 登出拒入）；四页面——登录页（预填 admin/admin123，非 ADMIN `message.error` 拒入）、短剧列表（封面缩略/状态 Tag/上下架/删除 Popconfirm/keyword 检索/分页）、短剧表单（封面上传直传回显）、剧集管理（视频上传 + 时长元数据自动读取）；`App.tsx` ConfigProvider zhCN + App.useApp。`pnpm exec tsc -b` 绿。

**门禁实证（浏览器自动化全绿，IAB 双标签页 3000/3001）**：
- 无 token 访问 `/dramas` → 跳 `/login`（表单预填）；demo/demo123 登录 → alert「该账号无后台权限」停留登录页；admin 登录 → 列表 6 行「共 6 部」。
- 空表单点「创建」→ 必填校验拦截（剧名/分类/封面三提示，未发请求）。
- 新增短剧「门禁演示剧」：封面经 DataTransfer 注入真实走 `/api/admin/files/upload`（新 uuid `69765cba…jpg`，按钮变「重新上传封面」）→「新增成功」共 7 部；id=8（自增已过此前门禁删除的测试行）。
- 剧集管理加两集：视频注入上传（`d713af8e…mp4` 等）→ 时长自动读取 20s/13s；剧集表 2 行。
- C 端首页出现「门禁演示剧 全 2 集」；播放页 `?ep=1` 视频 paused=false、readyState=4、2.8s/20s 真实播放中，选集按钮 1/2 渲染。
- 下架 → Tag 变「下架」、C 端 6 卡片不含该剧；上架 → C 端重现；删除（Popconfirm okText「删 除」）→ 共 6 部、C 端 `/drama/8` 显示「短剧不存在或已下架」。
- 分页：curl 造 5 部至 11 条 → 第 1 页 10 行（id 倒序）+ 第 2 页 1 行；清理后恢复 6 部种子态。

**偏差记录**：
1. **antd 实际版本 6.6.6**（计划写 AntD 5）：脚手架阶段即装 6.x，API 与计划用到的组件兼容，未降级。两处 v6 口径适配：`UploadRequestOption` 不再从 `antd/es/upload/interface` 导出（TS 报错），customRequest 改为内联箭头函数靠上下文类型推断；Modal 关闭销毁用 `destroyOnHidden`。
2. **计划外新增 `GET /api/admin/dramas/{id}`**：编辑页回填需要单查接口，原计划仅有列表接口，补齐（属于计划遗漏而非范围膨胀）。
3. Upload 组件未走 onSuccess/onError 回调链——上传状态（URL 回填、按钮文案、时长探测）由自定义 handler 自管，行为等价、代码更直。

## 2026-10-16 19:30 · 阶段3 收口：互动闭环与管理后台全部完成

- 验收清单 8/8 勾选（`docs/plan/第三阶段-互动闭环与管理后台.md` §六）；断网项以资源审计代验（三关键页 performance 条目全部本机 + 源码 grep 无外链），未真实拔网线复测。
- 测试口径：`./mvnw test` 27/0 全绿；web、admin 两端 `pnpm exec tsc -b` 绿。
- 题目必含功能对照：注册/登录 ✅、内容展示 ✅、点赞 ✅、收藏 ✅、管理后台增删改查 ✅——现场演示主路径全部真实打通（无 mock）。
- 门禁用临时数据已清理：库回到 6 剧/24 集/2 账号种子态；`uploads/files/` 留有门禁期上传产物（uuid 文件，磁盘保留决策内）。
- 遗留（记录不修，计划外）：C 端未匹配路由返回 500 而非 404（Spring 默认行为，影响面仅控制台日志，不影响演示页面）；下阶段若做体验加分项可顺手收。

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

## 2026-10-17 · 阶段4 合并回 main（冲突 6 处按双方保留解，门禁全绿）

第三阶段收口上 main（5ff846f）后，按第四阶段计划 §四拍板的合并时机执行合并：worktree 分支先 merge main 解冲突（cc6fb0a），main 再 fast-forward 并入。

### 冲突解决（6 处）

1. `WatchProgressRepository`（add/add）：阶段4 的 findByUserIdAndDramaId/findByUserIdOrderByUpdatedAtDesc（续播/历史）+ 阶段3 的 deleteByDramaId/deleteByEpisodeId（级联清理）合并为一个接口，注释标注各自归属。
2. `DramaService.listOnline`：阶段4 的 keyword 参数搜索 × 阶段3 的 `DramaSummary::from` 口径融合（私有 toSummary 已被阶段3 移除）。
3. `DramaController.listOnline`：`ApiResponse<List<DramaSummary>>` 统一包体 + keyword 透传。
4. `DramaRepository`：C 端搜索查询（阶段4）与后台分页检索（阶段3）两方法并存，注释区分用途。
5. `PlayerPage.tsx`：换用阶段3 的 `useDramaInteractions(detail, onChange)` 新签名——播放页点赞收藏由「占位提示」升级为真实写库；阶段4 的进度/手势/连播状态全部保留。
6. `docs/devlog.md`：阶段3/阶段4 条目按时间序并存。

### 合并适配（非冲突、为对齐阶段3 架构决策）

- `ProgressController` 响应改 `ApiResponse.ok()` 包装（阶段3 定了「全部 /api 接口统一包体」，前端 client.ts 拦截器统一拆包后页面层 `res.data` 仍是业务数据，阶段4 前端零适配）。

### 合并后门禁（全绿）

- `./mvnw test` 35 例全绿（阶段3 27 + 阶段4 8）；web、admin `pnpm exec tsc -b` 绿。
- 8081（worktree 合并版）与 8080（main 重启后）双侧冒烟：列表统一包体 6 剧、keyword=豪门 命中 1 部、进度上报 upsert、history 倒序、阶段3 点赞幂等切换与收藏列表共存正常。
- 8080 演示主路径已重启加载合并后代码（前端 3000 经 vite HMR 自动更新）。
- worktree 已清理（8081/3002 停止、`git worktree remove`）；分支 `feat/m4-experience` 保留可查。
