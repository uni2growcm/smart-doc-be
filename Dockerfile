# ────────────────────────────────────────────────────────────────
# Stage 1: Build React + Spring Boot JAR with Gradle + Java 25
# ────────────────────────────────────────────────────────────────
FROM gradle:jdk25-noble AS builder

WORKDIR /app

COPY . .

RUN gradle clean bootJar --no-daemon -Pprofile=docker

RUN find build/libs/ -name "*.jar" ! -name "*-plain.jar"  -exec cp {} build/libs/app.jar \;

# ────────────────────────────────────────────────────────────────
# Stage 2: Lightweight runtime image with Java 25 JRE
# ────────────────────────────────────────────────────────────────
FROM eclipse-temurin:25-jre AS runtime

WORKDIR /app

COPY --from=builder /app/build/libs/app.jar .

EXPOSE 8080

ENTRYPOINT ["java",\
  "-XX:+UseContainerSupport",\
  "-XX:+UseG1GC",\
  "-XX:+UseStringDeduplication",\
  "-XX:+OptimizeStringConcat",\
  "-XX:+HeapDumpOnOutOfMemoryError",\
  "-jar",\
  "app.jar"\
]