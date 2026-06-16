FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY pom.xml checkstyle.xml ./
RUN mvn dependency:go-offline -B --no-transfer-progress -q
COPY src ./src
RUN mvn package -DskipTests -B --no-transfer-progress -q && \
    cp target/arenahub-backend-*.jar target/app.jar

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app
USER app
COPY --from=builder /app/target/app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", \
  "-Xmx256m", \
  "-XX:MaxMetaspaceSize=128m", \
  "-XX:+UseContainerSupport", \
  "-jar", "app.jar"]
