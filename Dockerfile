FROM maven:3.10-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn --batch-mode package && cp target/alerta-enchentes-*.jar /app.jar

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app.jar ./app.jar
USER 10001
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "exec java -jar /app/app.jar --server.port=${SERVER_PORT:-${PORT:-8080}}"]
