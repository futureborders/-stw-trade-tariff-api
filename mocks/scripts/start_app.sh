#!/bin/bash -e

if [[ -z $DEPLOYMENT_ENVIRONMENT  ]]
then
  echo "Warning: \$DEPLOYMENT_ENVIRONMENT not set (e.g. (application)-stubbed-functional)"
fi

JAVA_MONITORING_OPTIONS="-Dcom.sun.management.jmxremote
             -Dcom.sun.management.jmxremote.port=1099
             -Dcom.sun.management.jmxremote.rmi.port=1099
             -Dcom.sun.management.jmxremote.local.only=false
             -Dcom.sun.management.jmxremote.authenticate=false
             -Dcom.sun.management.jmxremote.ssl=false
             -Djava.rmi.server.hostname=127.0.0.1
             -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=2005"

export JAVA_OPTIONS="-XX:MaxMetaspaceSize=128m -XX:CompressedClassSpaceSize=32m -XX:ReservedCodeCacheSize=64m -Xss256k -XX:NativeMemoryTracking=summary -XX:+UnlockDiagnosticVMOptions $JAVA_MONITORING_OPTIONS"

WIREMOCK_OPTS="--https-port 8443 --port 9090 --container-threads=200 --jetty-acceptor-threads=50"

if [[ $DEPLOYMENT_ENVIRONMENT == *"-stubbed-nft" ]]; then
    echo "Starting mocks with request journal DISABLED"
    WIREMOCK_OPTS="$WIREMOCK_OPTS --no-request-journal"
else
    echo "Starting mocks with request journal ENABLED"
fi

exec /start_java.sh -c "-cp wiremock-standalone.jar com.github.tomakehurst.wiremock.standalone.WireMockServerRunner $WIREMOCK_OPTS"
