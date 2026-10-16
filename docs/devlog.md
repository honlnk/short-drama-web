# 施工日志（devlog）

格式：日期 + 阶段 + 验收结论 + 偏差记录。每阶段门禁时更新。

## 2026-10-16 · 阶段0 脚手架（进行中）

- 环境核实：Java 21.0.9 (Temurin) / Maven 3.9.11 / Node v24.11.0 / pnpm 10.21.0 / git 2.51.2；start.spring.io 与 npm registry 均可达。
- `git init -b main` 完成；计划定稿写入 `docs/PLAN.md`，含与 PRD 的冲突裁定（目录端口按 apps/* + 3000/3001；演示账号 demo 按 demo123）。
- 待办：apps/api（Spring Initializr）、apps/web、apps/admin（Vite 模板）、scripts/dev.sh、三服务门禁验收。
