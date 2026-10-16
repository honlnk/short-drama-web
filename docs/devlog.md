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
