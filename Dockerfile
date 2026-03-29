FROM gradle:8.5-jdk21 AS build
WORKDIR /app
COPY . .

RUN chmod +x gradlew

RUN ./gradlew bootJar -x test

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]