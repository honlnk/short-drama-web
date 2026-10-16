#!/bin/bash
# 从 Mixkit 下载竖屏（9:16）种子视频素材到 seed-assets/videos/
# 用法: ./scripts/fetch-seed-media.sh [数量]   默认 50
# 幂等：已存在且非空的文件跳过；下载失败的留在 .fail 清单中。
set -u
cd "$(dirname "$0")/.."

COUNT="${1:-50}"
DIR="seed-assets/videos"
IDS_FILE="scripts/mixkit-ids.txt"
mkdir -p "$DIR"

# 已验证可直连下载的 ID 清单（243 个，取自 mixkit.co/free-vertical-videos 前 3 页）
if [ ! -f "$IDS_FILE" ]; then
  echo "缺少 $IDS_FILE" >&2; exit 1
fi

ok=0; skip=0; fail=0
: > /tmp/mixkit.fail
i=0
while read -r id && [ "$ok" -lt "$COUNT" ]; do
  i=$((i+1))
  f="$DIR/mixkit-$id.mp4"
  if [ -s "$f" ]; then skip=$((skip+1)); ok=$((ok+1)); continue; fi
  url="https://assets.mixkit.co/videos/$id/$id-360.mp4"
  code=$(curl -sL --max-time 30 -w "%{http_code}" -o "$f" "$url")
  if [ "$code" = "200" ] && [ -s "$f" ]; then
    ok=$((ok+1)); printf "\r[%d/%d] ok=%d skip=%d fail=%d" "$i" "$COUNT" "$ok" "$skip" "$fail"
  else
    fail=$((fail+1)); echo "$id" >> /tmp/mixkit.fail; rm -f "$f"
  fi
done < "$IDS_FILE"

echo
echo "完成: 成功/跳过共 $ok 个, 失败 $fail 个（清单 /tmp/mixkit.fail）"
echo "文件数: $(ls "$DIR"/*.mp4 2>/dev/null | wc -l | tr -d ' '), 总体积: $(du -sh "$DIR" 2>/dev/null | cut -f1)"
