# 短剧 APP 项目架构

## 项目结构

- `README.md` — 项目说明（仓库入口）
- `AGENTS.md` — AI 协作约定（供编码工具读取，固定在根目录）
- `apps/` — 三个独立工程
  - `api/` — 后端服务（Spring Boot 3.5，端口 8080）
  - `web/` — 用户端（Vite + React，端口 3000）
  - `admin/` — 管理后台（Vite + React + Ant Design，端口 3001）
- `docs/` — 项目文档
  - `REQUIREMENTS.md` — 题面原文（需求唯一来源）
  - `PRD.md` — 产品需求文档
  - `ARCHITECTURE.md` — 本文档
  - `PLAN.md` — 实施计划与执行裁定
  - `devlog.md` — 施工日志
  - `plan/` — 分阶段细化计划
- `scripts/` — 脚本工具（`dev.sh` 一键启动等）
- `logs/` — 运行日志（不入库）

## 技术栈

| 模块 | 技术 | 端口 |
|------|------|------|
| 用户端 web | React 18 + Vite + Tailwind CSS | 3000 |
| 管理后台 admin | React 18 + Vite + Tailwind CSS | 3001 |
| 后端 api | Spring Boot 3 + JPA + MySQL | 8080 |

## 核心功能

### 用户端 web
- 短剧列表浏览（首页推荐、分类筛选）
- 短剧详情与播放
- 用户注册 / 登录（JWT）
- 点赞、收藏

### 管理后台 admin
- 短剧内容 CRUD（标题、封面、视频、分类、描述）
- 用户管理（查看、禁用）
- 数据统计（可选）

### 后端 api
- RESTful API 设计
- Spring Security + JWT 认证
- JPA + MySQL（本机已安装，3306 直连，专用库 `short_drama`）
- 文件上传（封面图、视频）

## 数据模型（核心表）

| 表 | 说明 |
|---|---|
| user | 用户（id, username, password, nickname, avatar） |
| drama | 短剧（id, title, cover, video_url, category, description, status） |
| episode | 剧集（id, drama_id, episode_num, video_url, duration） |
| favorite | 收藏（id, user_id, drama_id, created_at） |
| like | 点赞（id, user_id, drama_id, created_at） |

## 开发原则

1. **单仓库 Monorepo**：所有代码在一个 Git 仓库，无子模块
2. **真实数据持久化**：本机 MySQL 存储，数据真实落库可交叉验证
3. **前后端分离**：API 与前端独立开发，通过 HTTP 通信
4. **同构前端**：web 和 admin 共用技术栈，可复用组件和工具函数

## 启动方式

```bash
# 后端（需先在本机 MySQL 创建数据库 `short_drama`，表结构与种子数据由应用启动时自动初始化）
cd apps/api && ./mvnw spring-boot:run

# 用户端
cd apps/web && pnpm dev

# 管理后台
cd apps/admin && pnpm dev
```

## 后续扩展方向

- 视频存储接入 OSS / CDN
- 短剧推荐算法
- 支付与会员体系
