#!/bin/bash

# 실행 중 에러 발생 시 종료
set -e

# 1. 현재 실행 중인 서비스 확인 (Blue가 켜져 있는지 확인)
# docker compose ps 명령어로 실행 중인 컨테이너 이름에 'blue'가 있는지 확인
IS_BLUE=$(docker compose -f /opt/focus-to-level-up/docker-compose.yml ps | grep app-blue || true)

if [ -z "$IS_BLUE" ]; then
  echo "### BLUE => Target is BLUE (현재 Green이거나 아무것도 없음) ###"
  TARGET_SERVICE="app-blue"
  TARGET_PORT="8081"
  STOP_SERVICE="app-green"
else
  echo "### GREEN => Target is GREEN (현재 Blue가 실행 중) ###"
  TARGET_SERVICE="app-green"
  TARGET_PORT="8082"
  STOP_SERVICE="app-blue"
fi

echo "### 2. $TARGET_SERVICE 배포 시작... ###"
docker compose -f /opt/focus-to-level-up/docker-compose.yml pull $TARGET_SERVICE
docker compose -f /opt/focus-to-level-up/docker-compose.yml up -d $TARGET_SERVICE

echo "### 3. Health Check 시작 (최대 60초 대기) ###"
# Actuator가 있다면 /actuator/health, 없다면 단순히 / 호출
# curl 옵션: -s(silent), -o(output null), -w(http code 출력)
for i in {1..10}; do
  # 로컬호스트의 타겟 포트로 요청을 보내서 응답 코드가 200인지 확인
  HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://127.0.0.1:$TARGET_PORT/actuator/health || true)

  if [ "$HTTP_CODE" -eq 200 ] || [ "$HTTP_CODE" -eq 401 ] || [ "$HTTP_CODE" -eq 403 ]; then
    echo "Health Check 성공! (HTTP Status: $HTTP_CODE)"
    break
  fi

  if [ $i -eq 10 ]; then
    echo "Health Check 실패... (HTTP Status: $HTTP_CODE)"
    echo "배포를 중단하고 새로 띄운 컨테이너를 종료합니다."
    docker compose -f /opt/focus-to-level-up/docker-compose.yml stop $TARGET_SERVICE
    exit 1
  fi

  echo "대기 중... ($i/10)"
  sleep 6
done

echo "### 4. Nginx 트래픽 전환 ($TARGET_PORT) ###"
# 변수가 아닌 static한 proxy_pass 구문을 파일에 덮어씀
echo "proxy_pass http://127.0.0.1:$TARGET_PORT;" | sudo tee /etc/nginx/conf.d/service-env.inc

# Nginx Reload
sudo nginx -s reload

echo "### 5. 이전 버전 ($STOP_SERVICE) 종료 ###"
docker compose -f /opt/focus-to-level-up/docker-compose.yml stop $STOP_SERVICE

echo "### 배포 완료! ###"
