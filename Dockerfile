FROM amazoncorretto:21-alpine-jdk AS build
WORKDIR /app
# Gradle cache
COPY gradle gradle
COPY build.gradle settings.gradle gradle.properties gradlew gradlew.bat ./
RUN ./gradlew dependencies --no-daemon
# Gradle build
COPY . .
RUN ./gradlew build -x test --no-daemon

FROM amazoncorretto:21-alpine-jdk
WORKDIR /app
COPY --from=build /app/build/libs/*SNAPSHOT.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]