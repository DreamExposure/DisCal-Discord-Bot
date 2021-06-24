FROM docker.io/adoptopenjdk/openjdk16:alpine-jre
MAINTAINER DreamExposure
COPY server/target/DisCal-Server.jar DisCal-Server.jar
ENTRYPOINT ["java", "-jar", "/DisCal-Server.jar"]
