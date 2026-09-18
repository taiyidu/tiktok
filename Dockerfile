#FROM eclipse-temurin:21-jdk-jammy
FROM ghcr.io/eclipse-temurin:21-jdk-jammy

ENV TZ=Asia/Shanghai

WORKDIR /app

COPY gongsheng-0.0.1-SNAPSHOT.jar gongsheng.jar

EXPOSE 8080

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=70.0", "-jar", "gongsheng.jar"]
