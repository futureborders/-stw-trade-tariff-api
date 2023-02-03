# "Check how to import or export goods" service - stw-trade-tariff-api

## About the service

Use this service to get information about importing and exporting goods for your business, including:

- how to register your business for trading
- which licences and certificates you need for your goods
- paying the right VAT and duties for your goods
- how to make declarations for your goods to clear the UK border
- which commodity codes you'll need to classify your goods

The live service is accessed via ```https://www.gov.uk/check-how-to-import-export```

## About this repository
- this is a backend micro service for the signposting service
- it's a Spring Boot application
- it depends on the content overlay service stwgs-content-api, hmrc-trade-tariff-api-client library for OTT API calls and the stw-prometheus-metrics for prometheus metrics.
- it uses docker containers for content api mock service and wiremock stub service for OTT(online trade tariff api)
- Source code formatted using the [Google Java formatting standards](https://google.github.io/styleguide/javaguide.html). There are plugins available for both IntelliJ and Eclipse which can be found [here](https://github.com/google/google-java-format).

## Installation

The following steps will enable you to setup your development environment:

* Set JAVA_HOME, PATH and MVN_HOME env variables
* Clone the stw-prometheus-metrics and hmrc-trade-tariff-api-client repositories
* Install stw-prometheus-metrics into local maven repository using: ```mvn clean install```
* Install hmrc-trade-tariff-api-client into local maven repository using: ```mvn clean install```
* Compile and run functional tests using mvn: ```mvn clean install```
* Build the project : ```mvn clean compile```
* Start the application in local : ```docker-compose up --b```

## Dependencies

* jdk - https://adoptium.net/en-GB/temurin/releases/?version=11
* mvn - https://maven.apache.org/download.cgi
* docker - https://www.docker.com/products/docker-desktop/

## Structure
It is a maven multi module project, it has two modules one is stw-trade-tariff-api-app and other is stw-trade-tariff-api-ft.

### stw-trade-tariff-api structure

| Directory                   | Description                                                                     |
|-----------------------------|---------------------------------------------------------------------------------|
| `mocks/`                    | Contains wiremock stubs for online trade tariff(OTT) api and stwgs-content-api. |
| `stw-trade-tariff-api-app/` | Service maven module.                                                           |
| `stw-trade-tariff-api-ft/`  | Functional tests maven module.                                                  | 

### stw-trade-tariff-api-app structure

| Directory               | Description                                                      |
|-------------------------|------------------------------------------------------------------|
| `src/main/resources/`   | Contains application configuration files (application.yml).      |
| `src/test/resources/`   | Contains application test configuration files (application.yml). |
| `src/main/java`         | Contains all the source code.                                    |
| `src/test/java/`        | Contains all the test code.                                      |

### stw-trade-tariff-api-ft structure

| Directory                     | Description                                                            |
|-------------------------------|------------------------------------------------------------------------|
| `src/test/resources/`         | Contains application test configuration files (application.yml).       |
| `src/test/resources/features` | Contains cucumber feature files.                                       |
| `src/test/java/`              | Contains cucumber step definitions, request and response pojo classes. |



## Licence

This application is made available under the [Apache 2.0 licence](/LICENSE).
