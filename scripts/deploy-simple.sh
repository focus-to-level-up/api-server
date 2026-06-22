#!/bin/bash

set -euo pipefail

SERVICE="${1:?Usage: deploy-simple.sh <service> [health-url] [started-log-pattern]}"
HEALTH_URL="${2:-}"
STARTED_LOG_PATTERN="${3:-Started}"
COMPOSE_FILE="${COMPOSE_FILE:-/opt/focus-to-level-up/docker-compose.yml}"

echo "🚀 $SERVICE 일반 배포 시작..."

echo "### 1. 이미지 Pull ###"
docker compose -f "$COMPOSE_FILE" pull "$SERVICE"

echo "### 2. 기존 컨테이너 종료 및 제거 ###"
docker compose -f "$COMPOSE_FILE" stop "$SERVICE" || true
docker compose -f "$COMPOSE_FILE" rm -f "$SERVICE" || true

echo "### 3. 새 컨테이너 실행 ###"
docker compose -f "$COMPOSE_FILE" up -d "$SERVICE"

if [ -n "$HEALTH_URL" ]; then
  echo "### 4. Health Check 시작 (최대 120초 대기) ###"
  for i in {1..24}; do
    HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$HEALTH_URL" || true)

    if [ "$HTTP_CODE" -eq 200 ]; then
      echo "✅ Health Check 성공! (HTTP Status: $HTTP_CODE)"
      echo "✅ $SERVICE 배포 완료!"
      exit 0
    fi

    if [ "$i" -eq 24 ]; then
      echo "❌ Health Check 실패... (HTTP Status: $HTTP_CODE)"
      docker compose -f "$COMPOSE_FILE" logs --tail=100 "$SERVICE"
      exit 1
    fi

    echo "⏳ 대기 중... ($i/24) - Res: $HTTP_CODE"
    sleep 5
  done
fi

echo "### 4. 컨테이너 상태 확인 ###"
sleep 10
RUNNING_SERVICE=$(docker compose -f "$COMPOSE_FILE" ps --status running --services | grep -x "$SERVICE" || true)

if [ -z "$RUNNING_SERVICE" ]; then
  echo "❌ $SERVICE 컨테이너가 실행 중이 아닙니다."
  docker compose -f "$COMPOSE_FILE" logs --tail=120 "$SERVICE"
  exit 1
fi

if ! docker compose -f "$COMPOSE_FILE" logs --tail=120 "$SERVICE" | grep -q "$STARTED_LOG_PATTERN"; then
  echo "❌ $SERVICE 시작 로그를 확인하지 못했습니다. 최근 로그를 출력합니다."
  docker compose -f "$COMPOSE_FILE" logs --tail=120 "$SERVICE"
  exit 1
fi

echo "✅ $SERVICE 배포 완료!"
