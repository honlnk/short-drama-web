# 短剧 Web APP（AI Coding 现场题）实施计划

依据：`REQUIREMENTS.md`（题目）+ `ARCHITECTURE.md`（已有架构决策，本计划遵循其中 monorepo / Spring Boot 3 + JPA + H2 / 双 React 前端 / JWT 的全部决定，仅将其具体化）。全程 UI 与文案为中文。

## 拍死的技术决策（无开放选择题）

| 决策点 | 定案 |
|---|---|
| 后端 | Spring Boot 3.5.x（start.spring.io 生成，Java 21、Maven + mvnw），依赖：web / data-jpa / security / validation / h2 / lombok / jjwt 0.12 |
| 数据库 | H2 **file 模式**（`jdbc:h2:file:./data/shortdrama`，重启不丢数据）；另附 `application-mysql.yml` + `docker-compose.yml`（专属 MySQL 8.0 容器映射 **3307**，不碰现有 linkseek-mysql），本期只验证 H2 路径 |
| 前端 | 两个独立 Vite + React + TypeScript + Tailwind CSS v4（@tailwindcss/vite）+ react-router + axios 应用；web 端口 3000、admin 端口 3001 |
| 种子视频 | 开发期从 test-videos.co.uk / samplelib.com（curl -L）下载 2-3 个小 mp4 复制为多份，存 `apps/api/uploads/videos/`，后端静态映射 `/uploads/**`；封面用本地 SVG（渐变+剧名）。兜底（若下载全失败）：种子改写远程 URL，功能不变仅演示依赖网络 |
| 种子账号 | admin/admin123（ADMIN）、demo/123456（USER）；8 部短剧 × 4-6 集，分类：都市、逆袭、古风、甜宠、悬疑 |
| 点赞/收藏语义 | POST `/api/dramas/{id}/like`、`/favorite` 为 toggle，返回 `{liked, likeCount}` / `{favorited}`；未登录返回 401，前端跳登录页 |
| UI 风格 | 仿红果短剧：深色顶栏 + 红色主色调 + 海报卡片网格；播放页为 video 标签 + 剧集列表 + 点赞/收藏按钮 |
| 上传 | admin 端 multipart 上传封面/视频 → 存 uploads/ 返回 URL；multipart 上限 200MB |

## 数据模型

- `user`（id, username 唯一, password bcrypt, nickname, role USER|ADMIN, status ACTIVE|DISABLED, createdAt）
- `drama`（id, title, coverUrl, category, description, status PUBLISHED|OFFLINE, likeCount, favoriteCount 冗余计数, createdAt/updatedAt）
- `episode`（id, dramaId FK, episodeNum, title, videoUrl, durationSeconds）
- `favorite`（user_id + drama_id 复合唯一）；`user_like`（同结构；**表名避开保留字 like**）

## API（REST，/api 前缀）

公开：`POST /api/auth/register|login`；`GET /api/dramas?category=&keyword=&page=&size=`；`GET /api/dramas/{id}`（含剧集 + 当前用户 liked/favorited 标记）；`/uploads/**`；`/h2-console/**`（演示时可打开给评委看表）。
认证：like/favorite toggle、`GET /api/me/favorites`。
ADMIN：`GET|POST /api/admin/dramas`、`PUT|DELETE /api/admin/dramas/{id}`、剧集 CRUD（`POST /api/admin/dramas/{id}/episodes`、`PUT|DELETE /api/admin/episodes/{id}`）、`GET /api/admin/users`、`PUT /api/admin/users/{id}/status`、`GET /api/admin/stats`、`POST /api/admin/upload`。
CORS 放行 localhost:3000 与 3001；JWT 存 localStorage，axios 拦截器带 Bearer。

## 阶段划分（每阶段有门禁，不过不开下一阶段）

1. **阶段0 脚手架**：`git init`；apps/api（Initializr 生成）、apps/web、apps/admin（Vite 模板）；`scripts/dev.sh` 一键启动三服务。门禁：三服务空跑可访问。
2. **阶段1 后端**：实体→仓库→JWT/Security→服务→控制器→种子数据（Java CommandLineRunner 写入，天然兼容 H2/MySQL 双方言）。门禁：curl 验收脚本（注册→登录→列表→详情→赞/藏→admin CRUD→禁用用户）全绿；重启后端验证 H2 文件持久化。
3. **阶段2 web 用户端**：布局+首页海报墙（分类 tab、搜索、分页）→ 播放页 → 登录/注册 → 我的收藏。门禁：browser-use 真实走通「注册→浏览→播放→点赞→收藏→收藏页」流程并截图。
4. **阶段3 admin 管理端**：登录（非 ADMIN 拒绝并提示）→ 统计仪表盘 → 短剧/剧集 CRUD（含封面上传）→ 用户管理（禁用后该用户登录被拒）。门禁：browser-use 走通完整 CRUD + 上传 + 禁用。
5. **阶段4 收尾**：README（启动说明+演示剧本）、AGENTS.md 回填实际命令、ARCHITECTURE.md 校正为实际状态、docs/devlog.md 施工日志补全、全链路终验。

## 验收点（全部可执行）

- 后端功能：curl 脚本逐项断言（脚本存 `scripts/api-test.sh` 可复跑）。
- 前端流程：browser-use 实操 + 截图留证。
- 持久化：杀后端重启，注册用户与点赞数据仍在。
- 一键演示：`scripts/dev.sh` 从冷启动到三服务可用。

## 明确不做（本期排除）

推荐算法、评论、观看历史、支付/会员、分集付费、HLS/转码、OSS/CDN、微信扫码登录、React Native/Flutter 端、国际化、高测试覆盖率（仅关键路径冒烟）、MySQL 深度联调（只交付配置与 compose 文件）。

## 迁移与替代

全新项目，无旧数据/旧机制，两项不适用。首个执行动作：将本计划定稿写入 `docs/PLAN.md` 作为施工事实来源。

## 文档同步

每阶段门禁时更新 `docs/devlog.md`（日期+阶段+验收结论+偏差）；结项时统一 git 提交；AGENTS.md「当前状态」节回填真实命令。最终汇报按规范含：未按计划实现 / 未验证清单 / 留下的尾巴 三节。
---

## 执行补记（2026-10-16，阶段0 开工时裁定）

计划与 PRD.md v1.0 存在两处冲突，裁定如下（依据：本计划自称「施工事实来源」且声明遵循 ARCHITECTURE.md；两票对一票；均为非用户体验类技术细节）：

1. **目录与端口**：PRD 7.2 写 `server/ + web/(5173) + admin/(5174)`，本计划与 ARCHITECTURE.md 均为 `apps/api + apps/web(3000) + apps/admin(3001)` → **按本计划执行**。PRD 5.2 端口本身标注为「如 5174」（示例性质）。
2. **演示账号密码**：PRD SD-01（产品需求，验收口径）写 `demo / demo123`，本计划写 `demo / 123456` → **按 PRD 执行：demo/demo123**（admin/admin123 两边一致无冲突）。此条供阶段1 实现种子账号时直接采用。

3. **uploads/ 是否入库**：阶段0 不裁断，留待阶段1 落种子媒体资源时决定（涉及二进制 mp4 是否入库）。
