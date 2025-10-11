# ---------- 1) Build stage ----------
FROM eclipse-temurin:23-jdk AS build
WORKDIR /src

# copy only wrapper + pom first to leverage cache
COPY mvnw ./
COPY .mvn .mvn
COPY pom.xml ./
RUN chmod +x mvnw && ./mvnw -q -DskipTests dependency:go-offline

# now copy sources and build
COPY src ./src
RUN ./mvnw -q -DskipTests package

# ---------- 2) Runtime stage ----------
FROM eclipse-temurin:23-jre
WORKDIR /app
COPY --from=build /src/target/*.jar app.jar
# Render provides $PORT; pass it to Spring Boot
CMD ["sh","-c","java -Dserver.port=$PORT -jar /app/app.jar"]
