# ============================================================
# Stage 1: Build the jar with Maven
# ============================================================
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy only the pom first so dependency downloads are cached
# separately from source changes (much faster rebuilds)
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B

# ============================================================
# Stage 2: Run the jar on a slim JRE (no Maven/JDK bloat)
# ============================================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Run as a non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring

COPY --from=build /app/target/expense-tracker-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]