FROM java:8

EXPOSE 8080

ADD target/springboot-mongo.jar springboot-mongo.jar

ENTRYPOINT ["java", "-jar", "springboot-mongo.jar"]
