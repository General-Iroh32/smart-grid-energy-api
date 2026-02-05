FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src src
RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:21-jre
RUN useradd --system --uid 10001 smartgrid
WORKDIR /app
COPY --from=build /workspace/target/smart-grid-energy-api-*.jar app.jar
USER smartgrid
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]

