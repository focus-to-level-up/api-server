#!/bin/bash

# 실행 중 에러 발생 시 종료
set -e

echo "🚀 Blue-Green 배포 시작..."

# 1. 현재 실행 중인 서비스 확인
IS_BLUE=$(docker compose -f /opt/focus-to-level-up/docker-compose.yml ps | grep app-blue || true)

if [ -z "$IS_BLUE" ]; then
  echo "### 현재: GREEN (또는 없음) => 배포 타겟: BLUE ###"
  TARGET_SERVICE="app-blue"
  TARGET_PORT="8081"
  STOP_SERVICE="app-green"
else
  echo "### 현재: BLUE => 배포 타겟: GREEN ###"
  TARGET_SERVICE="app-green"
  TARGET_PORT="8082"
  STOP_SERVICE="app-blue"
fi

echo "### 2. $TARGET_SERVICE 이미지 Pull 및 실행... ###"
docker compose -f /opt/focus-to-level-up/docker-compose.yml pull $TARGET_SERVICE
docker compose -f /opt/focus-to-level-up/docker-compose.yml up -d $TARGET_SERVICE

echo "### 3. Health Check 시작 (최대 150초 대기) ###"
# 반복 횟수를 10 -> 30으로 늘림 (5초 * 30회 = 150초)
for i in {1..30}; do
  HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://127.0.0.1:$TARGET_PORT/actuator/health || true)

  # 200 OK가 나오면 성공
  if [ "$HTTP_CODE" -eq 200 ]; then
    echo "✅ Health Check 성공! (HTTP Status: $HTTP_CODE)"
    break
  fi

  # 마지막 시도까지 실패하면
  if [ $i -eq 30 ]; then
    echo "❌ Health Check 실패... (HTTP Status: $HTTP_CODE)"
    echo "🔍 실패 원인 파악을 위해 컨테이너 로그를 출력합니다:"
    # 실패 시 컨테이너 로그를 찍어서 왜 안 떴는지 확인
    docker compose -f /opt/focus-to-level-up/docker-compose.yml logs --tail=50 $TARGET_SERVICE

    echo "배포를 중단하고 새로 띄운 컨테이너를 종료합니다."
    docker compose -f /opt/focus-to-level-up/docker-compose.yml stop $TARGET_SERVICE
    exit 1
  fi

  echo "⏳ 대기 중... ($i/30) - Res: $HTTP_CODE"
  sleep 5
done

echo "### 4. Nginx 트래픽 전환 ($TARGET_PORT) ###"
# Nginx 설정 변경
echo "proxy_pass http://127.0.0.1:$TARGET_PORT;" | sudo tee /etc/nginx/conf.d/service-env.inc

# Nginx 설정 문법 검사 및 Reload
if sudo nginx -t; then
    sudo nginx -s reload
else
    echo "❌ Nginx 설정 오류! 배포를 중단합니다."
    docker compose -f /opt/focus-to-level-up/docker-compose.yml stop $TARGET_SERVICE
    exit 1
fi

echo "### 트래픽 전환 완료. 기존 연결 처리를 위해 10초 대기... ###"
sleep 10

echo "### 5. 이전 버전 ($STOP_SERVICE) 종료 ###"
docker compose -f /opt/focus-to-level-up/docker-compose.yml stop $STOP_SERVICE

echo "✅ 배포 완료!"
