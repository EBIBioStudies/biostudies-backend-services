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
cd $(dirname $0)

# Obtain the process id by checking any process running with the application name
PID=$(pgrep -f APP_NAME)
JAVA_HOME=/nfs/ma/home/java/zulu8.38.0.13-ca-jdk8.0.212-linux_x64

# Kill and wait for process to be finished
while $(kill -9 ${PID} 2>/dev/null); do sleep 1;done;
rm -rf start_logs.txt

## deploy new version of application
nohup ${JAVA_HOME}/bin/java JVM_PARAMS \
    -Dsun.jnu.encoding=UTF-8 \
    -Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=DEBUG_PORT,suspend=n \
    -jar APP_PATH/APP_NAME \
    --spring.config.location=classpath:/application.yml,APP_PATH/application.yml \
    --server.port=APP_PORT >> start_logs.txt &
