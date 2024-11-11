FROM maven:3.9-eclipse-temurin-17-alpine

ENV BASE_DIR=/opt/agp \
    MAVEN_CONFIG=/opt/agp/.m2

RUN adduser -D -h $BASE_DIR -u 1000 inituser

WORKDIR $BASE_DIR
USER inituser

CMD ["mvn", "package"]
