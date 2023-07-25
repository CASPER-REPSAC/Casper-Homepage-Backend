FROM openjdk:17-oracle

EXPOSE 1234
ADD ./build/libs/*.jar app.jar

CMD ["java", "-jar", "app.jar"]