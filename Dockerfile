FROM openjdk:17-oracle

EXPOSE 8080
ADD ./build/libs/*.jar app.jar
ADD ./backup /home

CMD ["java", "-jar", "app.jar"]
