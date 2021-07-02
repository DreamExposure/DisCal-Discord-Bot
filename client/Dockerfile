FROM docker.io/adoptopenjdk/openjdk16:alpine-jre
MAINTAINER DreamExposure
COPY target/DisCal-Client.jar DisCal-Client.jar

ENTRYPOINT java ${JVM_ARGS} -jar /DisCal-Client.jar
