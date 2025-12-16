FROM gradle:7.6-jdk17 as builder
WORKDIR /build

COPY build.gradle settings.gradle /build/
RUN gradle dependencies --no-daemon

COPY src /build/src
RUN gradle build -x test --parallel

FROM openjdk:17.0.1-slim
WORKDIR /app

COPY --from=builder /build/build/libs/*-SNAPSHOT.jar ./app.jar

EXPOSE 8080

USER nobody
ENTRYPOINT ["java", "-jar", "-Duser.timezone=Asia/Seoul", "-Dsun.net.inetaddr.ttl=0", "app.jar"]
