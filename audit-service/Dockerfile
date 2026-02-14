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

COPY --from=build /src/target/*.jar app.jar

RUN addgroup --system spring && adduser --system spring --ingroup spring
USER spring:spring

EXPOSE 8080

ENTRYPOINT ["sh","-c","java -Dserver.port=${PORT:-8080} -jar /app/app.jar"]
