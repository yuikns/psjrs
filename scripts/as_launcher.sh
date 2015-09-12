#!/bin/sh
IO_CONF='production'
HTTP_PORT=9000
MY_JAVA_OPTS="-Xms20g -Xmx20g -XX:+UseConcMarkSweepGC -XX:ParallelCMSThreads=16 -XX:ParallelGCThreads=16 "
env IO_CONF=$IO_CONF JAVA_OPTS="$MY_JAVA_OPTS" argcv-scholar-current/bin/argcv-scholar -Dconfig.resource=application.prod.conf -Dhttp.port=$HTTP_PORT -Dcom.sun.management.jmxremote.port=9527 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false

