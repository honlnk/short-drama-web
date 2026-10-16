# 短剧 APP 项目架构

## 项目结构

```
short-drama-web/
├── REQUIREMENTS.md          # 需求文档
├── ARCHITECTURE.md          # 本文档
├── apps/
│   ├── web/                 # 用户端（React）
│   ├── admin/               # 管理后台（React）
│   └── api/                 # 后端服务（Spring Boot）
├── docs/                    # 项目文档
└── scripts/                 # 脚本工具
```

## 技术栈

| 模块 | 技术 | 端口 |
|------|------|------|
| 用户端 web | React 18 + Vite + Tailwind CSS | 3000 |
| 管理后台 admin | React 18 + Vite + Tailwind CSS | 3001 |
| 后端 api | Spring Boot 3 + JPA + H2 | 8080 |

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
- JPA + H2 内嵌数据库（免安装，启动即跑）
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
2. **快速演示优先**：H2 内嵌数据库，零外部依赖启动
3. **前后端分离**：API 与前端独立开发，通过 HTTP 通信
4. **同构前端**：web 和 admin 共用技术栈，可复用组件和工具函数

## 启动方式

```bash
# 后端（H2 数据库自动初始化）
cd apps/api && ./mvnw spring-boot:run

# 用户端
cd apps/web && pnpm dev

# 管理后台
cd apps/admin && pnpm dev
```

## 后续扩展方向

- 数据库切换 MySQL（配置化）
- 视频存储接入 OSS / CDN
- 短剧推荐算法
- 支付与会员体系
