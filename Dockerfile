FROM openjdk:8-jre-alpine
VOLUME /tmp
VOLUME /var/run/docker.sock

ARG JAR_FILE=app.jar
COPY ${JAR_FILE} app.jar

ENV STATIC_JAVA_OPTS="-Djava.security.egd=file:/dev/./urandom" \
  JAVA_OPTS=""

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $STATIC_JAVA_OPTS $JAVA_OPTS -jar app.jar"]
