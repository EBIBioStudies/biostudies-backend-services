#!/usr/bin/env bash
docker run -d \
  --hostname biostudies-rabbitmq \
  --name biostudies-rabbitmq \
  -e RABBITMQ_DEFAULT_USER=manager \
  -e RABBITMQ_DEFAULT_PASS=manager-local \
  -p 4369:4369 -p 5672:5672 -p 15672:15672 -p 25672:25672 \
  rabbitmq:3-management;
