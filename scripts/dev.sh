#!/usr/bin/env bash
# 一键启动短剧 APP 三服务：后端 api(8080) + 用户端 web(3000) + 管理后台 admin(3001)
# 日志统一写入 logs/ 目录；Ctrl-C 退出时会杀掉全部三个子进程。
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
LOG_DIR="${ROOT}/logs"
mkdir -p "${LOG_DIR}"

pids=()

cleanup() {
  echo ""
  echo "[dev.sh] 正在停止全部服务…"
  for pid in "${pids[@]:-}"; do
    kill "${pid}" 2>/dev/null || true
  done
  wait 2>/dev/null || true
  echo "[dev.sh] 已全部退出"
}
trap cleanup EXIT INT TERM

echo "[dev.sh] 日志目录: ${LOG_DIR} -> api.log / web.log / admin.log"

echo "[1/3] 启动后端 api -> http://localhost:8080"
(cd "${ROOT}/apps/api" && exec ./mvnw spring-boot:run) >"${LOG_DIR}/api.log" 2>&1 &
pids+=($!)

echo "[2/3] 启动用户端 web -> http://localhost:3000"
(cd "${ROOT}/apps/web" && exec pnpm dev) >"${LOG_DIR}/web.log" 2>&1 &
pids+=($!)

echo "[3/3] 启动管理后台 admin -> http://localhost:3001"
(cd "${ROOT}/apps/admin" && exec pnpm dev) >"${LOG_DIR}/admin.log" 2>&1 &
pids+=($!)

echo ""
echo "[dev.sh] 三服务已后台启动。快捷验证："
echo "  curl http://localhost:8080/api/ping   # 后端"
echo "  open http://localhost:3000            # 用户端"
echo "  open http://localhost:3001            # 管理后台"
echo "Ctrl-C 一键全部停止。实时日志: tail -f ${LOG_DIR}/<name>.log"
echo ""

wait
