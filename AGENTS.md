# AGENTS.md — short-drama-web

## 项目背景

AI Coding 现场实战题：开发一个类似「红果短剧」的简化版短剧 Web APP。需求唯一来源是 `docs/REQUIREMENTS.md`，动工前必读。这是一道限时现场题，最终需要现场演示运行并讲解设计思路。

## 硬性技术约束（题目规定，不可替换）

- 后端必须使用 **Java**
- 前端必须使用 **React**（React Native / Flutter 仅为可选加分项，默认不做）
- 必含功能：用户注册/登录、短剧内容展示、点赞、收藏、管理后台（短剧内容增删改查）
- 前后端及数据库必须**真实打通**，禁止用 mock 数据糊弄演示路径
- 项目必须可在本地正常启动并现场演示

## 当前状态（阶段0 脚手架已完成，2026-10-16）

三工程 monorepo 已就绪并通过阶段0门禁（三服务启动、双前端 Vite 代理链路连通、后端真实连上 MySQL）。实施计划与裁定见 `docs/PLAN.md`，施工日志见 `docs/devlog.md`。后续按计划推进：阶段1 后端业务 → 阶段2 web 用户端 → 阶段3 admin 管理端 → 阶段4 收尾。

### 构建 / 启动 / 测试命令

| 操作 | 命令 |
|---|---|
| 一键启动三服务 | `./scripts/dev.sh`（日志写 `logs/`，Ctrl-C 全部停止） |
| 后端单独启动 | `cd apps/api && ./mvnw spring-boot:run`（:8080） |
| 用户端 web | `cd apps/web && pnpm dev`（:3000） |
| 管理后台 admin | `cd apps/admin && pnpm dev`（:3001） |
| 后端构建 / 测试 | `cd apps/api && ./mvnw package` / `./mvnw test` |
| 前端类型检查 | `cd apps/web && pnpm exec tsc -b`（admin 同） |

### 目录结构与分层规则

- `apps/api`：Spring Boot 3.5.16（Java 21、Maven mvnw）。包 `com.shortdrama.api`：现有 `config/`（SecurityConfig 脚手架临时全放行、WebMvcConfig 映射 `/uploads/**`）、`controller/`（`GET /api/ping`）；阶段1 起按 `entity/`、`repository/`、`service/`、`controller/`、`dto/` 分层。
- `apps/web`：C 端，Vite + React + TS + Tailwind v4 + react-router + axios；`/api`、`/uploads` 经 Vite 代理转发 8080。
- `apps/admin`：管理后台，同栈 + Ant Design，仅 PC；代理同上。
- `apps/api/uploads/`：封面/视频本地存储目录（`/uploads/**` 静态服务）。
- `docs/`：plan/（分阶段实施计划，现行计划唯一存放处）、devlog.md（施工日志）。

### 数据库连接与初始化

- 本机 MySQL 26.7（homebrew 原生安装，`127.0.0.1:3306`，**非容器**；docker 里的 linkseek-mysql 与本项目无关），专用库 `short_drama`（utf8mb4_unicode_ci，已建好）。
- 凭据 `root / root123`，配置在 `apps/api/src/main/resources/application.yml`（本地演示项目，刻意不拆环境变量）。
- 建表方式：JPA `ddl-auto=update` 启动自动建表；种子数据将由阶段1 的 CommandLineRunner 空库自动导入。无需手工执行任何 SQL。

## 工作约定

- 演示优先：所有改动必须保证 `启动后端 → 启动前端 → 现场演示` 这条路径始终可用，破坏可运行性的改动优先修复
- 面向评委演示的关键页面（短剧列表/播放、登录注册、管理后台）保持整洁可用
- 初始账号、种子数据（至少几部可播放的短剧）要随项目提供，避免演示时现造数据
