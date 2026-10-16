# AGENTS.md — short-drama-web

## 项目背景

AI Coding 现场实战题：开发一个类似「红果短剧」的简化版短剧 Web APP。需求唯一来源是 `docs/REQUIREMENTS.md`，动工前必读。这是一道限时现场题，最终需要现场演示运行并讲解设计思路。

## 硬性技术约束（题目规定，不可替换）

- 后端必须使用 **Java**
- 前端必须使用 **React**（React Native / Flutter 仅为可选加分项，默认不做）
- 必含功能：用户注册/登录、短剧内容展示、点赞、收藏、管理后台（短剧内容增删改查）
- 前后端及数据库必须**真实打通**，禁止用 mock 数据糊弄演示路径
- 项目必须可在本地正常启动并现场演示

## 当前状态（阶段1 数据底座与种子资源已完成，2026-10-16）

三工程 monorepo 就绪（阶段0 门禁）+ 数据底座全线落地（阶段1 门禁，验收清单见 `docs/plan/第一阶段-数据底座与种子资源.md` 第五节）。当前能力：空库启动自动建 6 表并灌种子（6 剧 24 集 + 2 账号），`GET /api/dramas` 出真实数据，C 端首页渲染封面网格，视频/封面本地资源可播。后续按里程碑推进：用户链路（注册/登录/JWT）→ 详情/播放页 → 点赞收藏 → 管理后台。施工日志见 `docs/devlog.md`。

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

- `apps/api`：Spring Boot 3.5.16（Java 21、Maven mvnw）。包 `com.shortdrama.api` 分层：`config/`（SecurityConfig 脚手架临时全放行、WebMvcConfig 映射 `/uploads/**`、DataSeeder 种子灌库）、`entity/`（User/Drama/Episode/Favorite/UserLike/WatchProgress，drama.status 取 ONLINE/OFFLINE）、`repository/`、`service/`（DramaService）、`controller/`（`GET /api/ping`、`GET /api/dramas`）、`dto/`（DramaSummary）。
- `apps/web`：C 端，Vite + React + TS + Tailwind v4 + react-router + axios；首页已接 `/api/dramas` 渲染封面网格；`/api`、`/uploads` 经 Vite 代理转发 8080。
- `apps/admin`：管理后台，同栈 + Ant Design，仅 PC；代理同上。
- `apps/api/uploads/`：封面/视频本地存储目录（`/uploads/**` 静态服务）；不入 git，由 scripts 两个脚本重建。
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
