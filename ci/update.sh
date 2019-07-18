#!/bin/bash

#
# Kill current deployed instance and start deployment script.
#
# Parameters:
#   APP_PATH: deploy configuration base path path.
#   APP_PORT: application port
#   APP_NAME: deploy jar artifact name, i.e. my-application.jar
#   DEBUG_PORT: application debug port
#
cd $(dirname $0)

# Obtain the process id by checking any process running in the expected port
PID=$(netstat -antp 2>/dev/null -tlnp | awk '{print $4,$7}' | awk '/:'APP_PORT' */ {split($NF,a,"/"); print a[1]}')
JAVA_HOME=/nfs/ma/home/java/zulu8.38.0.13-ca-jdk8.0.212-linux_x64

# Kill and wait for process to be finished
while $(kill -9 ${PID} 2>/dev/null); do sleep 1;done;
rm -rf logs.txt

## deploy new version of application
nohup ${JAVA_HOME}/bin/java \
    -Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=DEBUG_PORT,suspend=n \
    -jar APP_PATH/APP_NAME \
    --spring.config.location=classpath:/application.yml,APP_PATH/application.yml \
    --server.port=APP_PORT >> logs.txt &
