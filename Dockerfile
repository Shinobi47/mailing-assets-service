FROM openjdk:11.0.11-jdk-slim
ARG APP_VERSION
#ARG can only be used in build time ENTRYPOINT is run time.
ENV APP_VERSION ${APP_VERSION}
MAINTAINER Haytam BENAYED
RUN echo "Jar version is ${APP_VERSION}"
COPY target/mailing-assets-service-${APP_VERSION}.jar /opt/app/mailing-assets-service-${APP_VERSION}.jar
WORKDIR /opt/app
ENTRYPOINT java -jar mailing-assets-service-${APP_VERSION}.jar
