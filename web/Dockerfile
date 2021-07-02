FROM docker.io/adoptopenjdk/openjdk16:alpine-jre
MAINTAINER DreamExposure
COPY target/DisCal-Web.jar DisCal-Web.jar

ENTRYPOINT java ${JVM_ARGS} -jar /DisCal-Web.jar
