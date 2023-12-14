#!/bin/bash

#
# Kill current deployed instance and start deployment script.
#
# Parameters:
#   APP PATH: deploy configuration base path path.
#   APP PORT: application port
#   APP NAME: deploy jar artifact name, i.e. my-application.jar
#   DEBUG PORT: application debug port
#
[ $(whoami) != "BS_USER" ] && echo "Must be BS_USER to run" && exit 1
[ $(hostname -s) != "HOST" ] && echo "Must be run on HOST" && exit 1
cd $(dirname $0)

# Obtain the process id by checking any process running with the application name
PID=$(pgrep -f APP_PATH/APP_NAME)
JAVA_HOME=/nfs/biostudies/.adm/java/zulu11.45.27-ca-jdk11.0.10-linux_x64

# Kill and wait for process to be finished
while $(kill -9 ${PID} 2>/dev/null); do sleep 1;done;
mv start_logs.txt logs/start_logs_$(date +%s).txt

## deploy new version of application
nohup ${JAVA_HOME}/bin/java JVM_PARAMS \
    -Dsun.jnu.encoding=UTF-8 \
    -Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=*:DEBUG_PORT,suspend=n \
    -jar APP_PATH/APP_NAME \
    --spring.config.location=APP_PATH/application.yml \
    --server.port=APP_PORT > start_logs.txt 2>&1 &
