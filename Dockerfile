FROM openjdk:11-slim as builder

WORKDIR /build

COPY ./ ./
RUN chmod +x mvnw
RUN ./mvnw package

FROM openjdk:11-slim

WORKDIR /app

COPY --from=builder /target/peeper-0.0.1-SNAPSHOT.jar ./peeper.jar

EXPOSE 8080
CMD ["java", "-jar", "peeper.jar"]
