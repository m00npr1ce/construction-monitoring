## Stage 1: build with Maven (faster & more reliable in CI)
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml ./
COPY src ./src

# Build with retries and without tests to speed up image build
RUN mvn -B -e -DskipTests \
    -Dmaven.wagon.http.retryHandler.count=3 \
    -Dmaven.wagon.httpconnectionManager.ttlSeconds=60 \
    clean package

## Stage 2: runtime image
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create uploads dir for attachments
RUN mkdir -p /app/uploads

# Copy built jar
COPY --from=build /app/target/backend-0.0.1-SNAPSHOT.jar /app/app.jar

EXPOSE 8080

ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]

