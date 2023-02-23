# Copyright 2021 Crown Copyright (Single Trade Window)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

FROM adoptopenjdk/openjdk11:jre-11.0.11_9-alpine
RUN apk -U upgrade
ENV PORT 8081
ENV CLASSPATH /opt/lib
EXPOSE 8081 8000 9000

ENV SSL_ROOT_CERT /root/rds-combined-ca-bundle.pem
COPY rds-combined-ca-bundle.pem ${SSL_ROOT_CERT}
# copy pom.xml and wildcards to avoid this command failing if there's no target/lib directory
COPY pom.xml target/lib* /opt/lib/

# NOTE we assume there's only 1 jar in the target dir
# but at least this means we don't have to guess the name
# we could do with a better way to know the name - or to always create an app.jar or something
COPY target/*.jar /opt/app.jar
WORKDIR /opt
CMD ["java", "-jar", "app.jar"]
