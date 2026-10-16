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
