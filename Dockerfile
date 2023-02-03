FROM adoptopenjdk/openjdk11:alpine-jre
RUN apk -U upgrade
ENV PORT 8081
ENV CLASSPATH /opt/lib
EXPOSE 8081 8000

# NOTE we assume there's only 1 jar in the target dir
# but at least this means we don't have to guess the name
# we could do with a better way to know the name - or to always create an app.jar or something
COPY stw-trade-tariff-api-app/target/*.jar /opt/app.jar
WORKDIR /opt
CMD ["java", "-jar", "app.jar"]
