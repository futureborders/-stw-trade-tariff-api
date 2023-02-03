#!/bin/bash

function usage() {
    echo 'Set JAVA_OPTIONS to override parameters passed to java. Excluding any GC related options.'
    echo 'Set GC_OPTIONS for any GC related parameters.'
    echo 'Set JMXEXPORTER_ENABLED to true to enable Prometheus JMX reporter agent'
    echo 'Usage: start_java.sh -c [command e.g -jar /data/app.jar server config.yml]'
    exit 1
}

#set -a
#eval $(/usr/local/bin/node-label-extractor)
#set +a

#Separated GC because if multiple GCs are passed to the JVM G1 is picked regardless of order
gc=${GC_OPTIONS:-"-XX:+UseConcMarkSweepGC"}
java_options=${JAVA_OPTIONS:-"-Xmx256m"}
other=${CORE_OPTIONS:-"-XX:NativeMemoryTracking=summary -XX:+UnlockDiagnosticVMOptions \
    -XX:+AlwaysPreTouch -XX:+PreserveFramePointer -XX:+DebugNonSafepoints -Djava.net.preferIPv4Stack=true"}

while getopts ":c:h" OPTION; do
    case $OPTION in
        c)
            command=$OPTARG
            ;;
        h)
            usage
            ;;
        :)
            echo "Option -$OPTARG requires an argument." >&2
            exit 1
            ;;
    esac
done

if [ -z "$command" ]
then
    usage
fi

if [ "$JMXEXPORTER_ENABLED" == true ]
then
    JMXEXPORTER_OPTIONS="-javaagent:$JMXEXPORTER_JAR=$JMXEXPORTER_PORT:$JMXEXPORTER_CONFIGFILE"
    echo "Prometheus JMX Exporter starting on :$JMXEXPORTER_PORT using config from $JMXEXPORTER_CONFIGFILE"
    echo "NOTE: Support for JMX Exporter is deprecated since java8:8.10.0. It will be removed in a future release."
fi

cmd="java $other $gc $java_options $JMXEXPORTER_OPTIONS $command"
echo $cmd
exec $cmd