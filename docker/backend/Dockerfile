FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /build
COPY server/pom.xml .
RUN mvn dependency:go-offline -B
COPY server/ .
RUN mvn package -DskipTests -B

FROM eclipse-temurin:17-jre-alpine
LABEL maintainer="IslandCampus Team"
RUN apk add --no-cache curl fontconfig ttf-dejavu
WORKDIR /app
COPY --from=builder /build/server-app/target/*.jar app.jar
RUN mkdir -p plugins screenshots uploads logs
EXPOSE 8080
ENTRYPOINT ["java", \
    "-Xms256m", "-Xmx512m", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-Duser.timezone=Asia/Shanghai", \
    "-jar", "app.jar"]
