FROM eclipse-temurin:21-jdk AS build
WORKDIR /src
COPY lesson/ .
RUN apt-get update && apt-get install -y maven && mvn -B -DskipTests -Pproduction package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /src/target/lesson-0.1.0.jar app.jar
ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
