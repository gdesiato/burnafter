# ---------- 1) Build stage ----------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /src

COPY mvnw ./
COPY .mvn .mvn
COPY pom.xml ./

RUN chmod +x mvnw && ./mvnw -B -DskipTests dependency:go-offline

COPY src ./src
RUN ./mvnw -B -DskipTests package

# ---------- 2) Runtime stage ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# OS packages + user first: these layers stay cached across code changes.
# (curl is used by the docker-compose healthcheck.)
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*
RUN addgroup --system spring && adduser --system spring --ingroup spring

# Only this layer changes when the code changes.
COPY --from=build /src/target/*.jar app.jar

USER spring:spring

EXPOSE 8080

ENTRYPOINT ["sh","-c","java -Dserver.port=${PORT:-8080} -jar /app/app.jar"]
