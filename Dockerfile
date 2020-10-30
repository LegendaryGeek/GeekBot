FROM gradle:4.7.0-jdk8-alpine AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle clean build --no-daemon 

FROM openjdk:8-jre-slim

COPY --from=build /home/gradle/src/build/distributions/GeekBot.tar /app/

WORKDIR /app
RUN tar -xvf GeekBot.tar

ENTRYPOINT ["java","-jar","/app/GeekBot/lib/GeekBot.jar"]
