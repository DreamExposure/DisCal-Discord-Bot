FROM docker.io/adoptopenjdk/openjdk16:alpine-jre
MAINTAINER DreamExposure
COPY target/DisCal-Server.jar DisCal-Server.jar

ENTRYPOINT java ${JVM_ARGS} -jar /DisCal-Server.jar
