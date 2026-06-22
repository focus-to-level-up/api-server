#!/bin/bash

set -euo pipefail

COMPOSE_FILE="${COMPOSE_FILE:-/opt/focus-to-level-up/docker-compose.yml}"
NGINX_INCLUDE="${NGINX_INCLUDE:-/etc/nginx/conf.d/service-env.inc}"
BLUE_SERVICE="${API_BLUE_SERVICE:-api-blue}"
GREEN_SERVICE="${API_GREEN_SERVICE:-api-green}"
LEGACY_BLUE_CONTAINER="${LEGACY_API_BLUE_CONTAINER:-focus-to-levelup-app-blue}"
LEGACY_GREEN_CONTAINER="${LEGACY_API_GREEN_CONTAINER:-focus-to-levelup-app-green}"
BLUE_PORT="${API_BLUE_PORT:-8081}"
GREEN_PORT="${API_GREEN_PORT:-8082}"
HEALTH_PATH="${API_HEALTH_PATH:-/actuator/health}"

echo "🚀 API Blue-Green 배포 시작..."

IS_NEW_BLUE_RUNNING=$(docker compose -f "$COMPOSE_FILE" ps --status running --services | grep -x "$BLUE_SERVICE" || true)
IS_LEGACY_BLUE_RUNNING=$(docker ps --format '{{.Names}}' | grep -x "$LEGACY_BLUE_CONTAINER" || true)

if [ -z "$IS_NEW_BLUE_RUNNING" ] && [ -z "$IS_LEGACY_BLUE_RUNNING" ]; then
  echo "### 현재: GREEN 또는 없음 => 배포 타겟: BLUE ###"
  TARGET_SERVICE="$BLUE_SERVICE"
  TARGET_PORT="$BLUE_PORT"
  STOP_SERVICE="$GREEN_SERVICE"
  STOP_LEGACY_CONTAINER="$LEGACY_GREEN_CONTAINER"
else
  echo "### 현재: BLUE => 배포 타겟: GREEN ###"
  TARGET_SERVICE="$GREEN_SERVICE"
  TARGET_PORT="$GREEN_PORT"
  STOP_SERVICE="$BLUE_SERVICE"
  STOP_LEGACY_CONTAINER="$LEGACY_BLUE_CONTAINER"
fi

echo "### 1. $TARGET_SERVICE 이미지 Pull 및 실행 ###"
docker compose -f "$COMPOSE_FILE" pull "$TARGET_SERVICE"
docker compose -f "$COMPOSE_FILE" up -d "$TARGET_SERVICE"

echo "### 2. Health Check 시작 (최대 150초 대기) ###"
for i in {1..30}; do
  HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "http://127.0.0.1:${TARGET_PORT}${HEALTH_PATH}" || true)

  if [ "$HTTP_CODE" -eq 200 ]; then
    echo "✅ Health Check 성공! (HTTP Status: $HTTP_CODE)"
    break
  fi

  if [ "$i" -eq 30 ]; then
    echo "❌ Health Check 실패... (HTTP Status: $HTTP_CODE)"
    docker compose -f "$COMPOSE_FILE" logs --tail=80 "$TARGET_SERVICE"
    docker compose -f "$COMPOSE_FILE" stop "$TARGET_SERVICE"
    exit 1
  fi

  echo "⏳ 대기 중... ($i/30) - Res: $HTTP_CODE"
  sleep 5
done

echo "### 3. Nginx 트래픽 전환 ($TARGET_PORT) ###"
echo "proxy_pass http://127.0.0.1:$TARGET_PORT;" | sudo tee "$NGINX_INCLUDE"

if sudo nginx -t; then
  sudo nginx -s reload
else
  echo "❌ Nginx 설정 오류! 배포를 중단합니다."
  docker compose -f "$COMPOSE_FILE" stop "$TARGET_SERVICE"
  exit 1
fi

echo "### 4. 이전 버전 ($STOP_SERVICE) 종료 ###"
sleep 10
docker compose -f "$COMPOSE_FILE" stop "$STOP_SERVICE" || true
docker stop "$STOP_LEGACY_CONTAINER" 2>/dev/null || true

echo "✅ API Blue-Green 배포 완료!"
