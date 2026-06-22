FROM gradle:8.10.2-jdk17 AS builder
WORKDIR /build

ARG GRADLE_TASK
ARG MODULE_DIR

COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle settings.gradle ./
COPY levelup-common levelup-common
COPY levelup-domain levelup-domain
COPY levelup-infra levelup-infra
COPY levelup-application levelup-application

RUN sh gradlew ${GRADLE_TASK} -x test --no-daemon

FROM amazoncorretto:17
WORKDIR /app

ARG MODULE_DIR

COPY --from=builder /build/${MODULE_DIR}/build/libs/*.jar ./app.jar

EXPOSE 8080
EXPOSE 9090

USER nobody
ENTRYPOINT ["java", "-jar", "-Duser.timezone=Asia/Seoul", "-Dsun.net.inetaddr.ttl=0", "app.jar"]
