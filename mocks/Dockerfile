FROM openjdk:11-jre-slim
ENV WIREMOCK_VERSION 2.27.2


WORKDIR /wiremock
ADD https://repo1.maven.org/maven2/com/github/tomakehurst/wiremock-standalone/${WIREMOCK_VERSION}/wiremock-standalone-${WIREMOCK_VERSION}.jar wiremock-standalone.jar

COPY 'mappings' mappings
COPY 'response-body' __files
COPY ./scripts/*.sh /

CMD ["/start_app.sh"]

EXPOSE 9090
