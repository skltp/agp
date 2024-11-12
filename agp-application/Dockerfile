FROM eclipse-temurin:17-jre-alpine

ENV BASE_DIR=/opt/agp/ \
    APP_USER=ind-app \
    LOG_DIR=/var/log/ \
    TZ=Europe/Stockholm \
    JARFILE=target/agp-application-*exec.jar

ADD ${JARFILE} ${BASE_DIR}/app.jar

RUN adduser -DH  -h ${BASE_DIR} -u 1000 ${APP_USER}

VOLUME ${BASE_DIR}/services

WORKDIR ${BASE_DIR}
USER ${APP_USER}
# A few args are default, but feel free to add more using JAVA_OPTS
CMD java -XX:MaxRAMPercentage=75 -Dloader.path="services/" ${JAVA_OPTS} -jar app.jar