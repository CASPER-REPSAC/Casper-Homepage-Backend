FROM openjdk:17-oracle

EXPOSE 8080
ADD ./build/libs/*.jar app.jar

CMD ["java", "-jar", "app.jar"]
