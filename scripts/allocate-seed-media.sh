#!/usr/bin/env bash
# 从 seed-assets/videos/ 按分类选片，落位到 apps/api/uploads/：
#   视频 uploads/videos/drama-{01..06}/ep-{01..04}.mp4（24 个）
#   封面 uploads/covers/drama-{01..06}.svg（6 张，渐变底 + 剧名 + 分类角标）
# 幂等：重复执行覆盖写，结果一致。最后输出校验报告（文件数 / 体积 / 非空 / 各集时长）。
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
SRC="${ROOT}/seed-assets/videos"
UPLOADS="${ROOT}/apps/api/uploads"
VIDEOS_DIR="${UPLOADS}/videos"
COVERS_DIR="${UPLOADS}/covers"

# 剧目 → 素材映射（ID 对应 seed-assets/README.md 清单；古装无实拍素材，按计划用氛围素材顶替）
# 格式：剧编号|mixkit-id|mixkit-id|mixkit-id|mixkit-id
MAP=(
  "01|2307|1221|1259|5088"     # 甜宠：情侣刷墙 / 海滩肖像 / 泳池肖像 / 喝汽水
  "02|1222|1232|1237|1240"     # 都市：跳跃特技 / 霓虹女孩 / 彩灯男人 / 跳舞男人
  "03|2284|3636|5121|2962"     # 逆袭：罚球练习 / 黑暗芭蕾 / 乐队演出 / 电台播音
  "04|22724|21603|3457|3995"   # 古装（氛围顶替）：哥特教堂 / 书页 / 黑暗火焰 / 暗物质
  "05|2376|1487|1597|3673"     # 悬疑：相机镜头 / 红蛙 / 工厂烟雾 / 手表机械
  "06|18219|2720|2721|10343"   # 甜宠：撑伞女孩 / 圣诞装饰球 / 装饰圣诞树 / 衣柜收纳
)

# 封面渐变配色（按分类）：编号|起色|终色|角标色
COVERS=(
  "01|#ec4899|#fdf2f8|#be185d" # 甜宠·玫粉
  "02|#0369a1|#e0f2fe|#075985" # 都市·蓝青
  "03|#c2410c|#7c3aed|#9a3412" # 逆袭·橙紫
  "04|#7f1d1d|#b45309|#450a0a" # 古装·绛金
  "05|#1e1b4b|#0f172a|#4c1d95" # 悬疑·暗紫
  "06|#e11d48|#ffe4e6|#9f1239" # 甜宠·桃红
)

TITLES=(
  [01]="闪婚老公是豪门" [02]="都市之最强狂兵" [03]="重生之逆袭人生"
  [04]="王爷的替嫁王妃" [05]="午夜出租车"   [06]="霸总的小娇妻"
)
CATEGORIES=([01]="甜宠" [02]="都市" [03]="逆袭" [04]="古装" [05]="悬疑" [06]="甜宠")

[[ -d "${SRC}" ]] || { echo "缺少 ${SRC}，先执行 ./scripts/fetch-seed-media.sh 50 下载素材"; exit 1; }

mkdir -p "${VIDEOS_DIR}" "${COVERS_DIR}"

# --- 1. 复制 24 个视频 ---
for row in "${MAP[@]}"; do
  IFS='|' read -r drama id1 id2 id3 id4 <<<"${row}"
  ep=1
  for id in "${id1}" "${id2}" "${id3}" "${id4}"; do
    src_file="${SRC}/mixkit-${id}.mp4"
    [[ -s "${src_file}" ]] || { echo "素材缺失：${src_file}"; exit 1; }
    dst="${VIDEOS_DIR}/drama-${drama}/ep-$(printf '%02d' "${ep}").mp4"
    mkdir -p "$(dirname "${dst}")"
    cp -f "${src_file}" "${dst}"
    ep=$((ep + 1))
  done
done

# --- 2. 生成 6 张封面 SVG（360×640 竖版）---
gen_cover() {
  local drama="$1" from="$2" to="$3" badge="$4"
  local title="${TITLES[$drama]}" cat="${CATEGORIES[$drama]}"
  local out="${COVERS_DIR}/drama-${drama}.svg"
  cat >"${out}" <<SVG
<svg xmlns="http://www.w3.org/2000/svg" width="360" height="640" viewBox="0 0 360 640">
  <defs>
    <linearGradient id="g" x1="0" y1="0" x2="1" y2="1">
      <stop offset="0" stop-color="${from}"/>
      <stop offset="1" stop-color="${to}"/>
    </linearGradient>
  </defs>
  <rect width="360" height="640" fill="url(#g)"/>
  <circle cx="52" cy="96" r="150" fill="#ffffff" opacity="0.10"/>
  <circle cx="330" cy="560" r="190" fill="#000000" opacity="0.14"/>
  <rect x="20" y="24" rx="12" width="$(( ${#cat} * 26 + 20 ))" height="34" fill="${badge}" opacity="0.92"/>
  <text x="$(( 20 + ( ${#cat} * 26 + 20 ) / 2 ))" y="47" font-family="PingFang SC, Hiragino Sans GB, sans-serif" font-size="19" fill="#ffffff" text-anchor="middle">${cat}</text>
  <text x="180" y="330" font-family="PingFang SC, Hiragino Sans GB, sans-serif" font-size="42" font-weight="700" fill="#ffffff" text-anchor="middle">${title}</text>
  <text x="180" y="384" font-family="PingFang SC, Hiragino Sans GB, sans-serif" font-size="17" fill="#ffffff" opacity="0.85" text-anchor="middle">全 4 集 · 竖屏短剧</text>
</svg>
SVG
}

for row in "${COVERS[@]}"; do
  IFS='|' read -r drama from to badge <<<"${row}"
  gen_cover "${drama}" "${from}" "${to}" "${badge}"
done

# --- 3. 校验报告 ---
fail=0
video_count=$(find "${VIDEOS_DIR}" -name '*.mp4' -type f | wc -l | tr -d ' ')
cover_count=$(find "${COVERS_DIR}" -name '*.svg' -type f | wc -l | tr -d ' ')
total_size=$(du -sh "${UPLOADS}" | cut -f1)

for f in "${VIDEOS_DIR}"/drama-*/ep-*.mp4; do
  [[ -s "$f" ]] || { echo "空文件：$f"; fail=1; }
done
for drama in 01 02 03 04 05 06; do
  [[ -s "${COVERS_DIR}/drama-${drama}.svg" ]] || { echo "封面缺失：drama-${drama}.svg"; fail=1; }
done
if [[ "${video_count}" -ne 24 || "${cover_count}" -ne 6 ]]; then
  echo "数量不符：视频 ${video_count}/24，封面 ${cover_count}/6"
  fail=1
fi

echo "=== 种子媒体校验报告 ==="
echo "视频：${video_count}/24   封面：${cover_count}/6   uploads 总体积：${total_size}"
echo "--- 各集素材与时长（duration_sec 供 DataSeeder 使用）---"
for drama in 01 02 03 04 05 06; do
  line="drama-${drama}:"
  for ep in 01 02 03 04; do
    f="${VIDEOS_DIR}/drama-${drama}/ep-${ep}.mp4"
    dur=$(mdls -name kMDItemDurationSeconds -raw "${f}" 2>/dev/null || echo 0)
    line+=" ep${ep}=${dur%.*}s"
  done
  echo "${line}"
done
[[ "${fail}" -eq 0 ]] && echo "校验通过" || { echo "校验失败"; exit 1; }
