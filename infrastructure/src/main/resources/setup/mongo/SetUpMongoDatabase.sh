#!/usr/bin/env bash
docker run -d \
  --name biostudies-mongo \
  -p 27017:27017 \
  -e MONGO_INITDB_ROOT_USERNAME=root \
  -e MONGO_INITDB_ROOT_PASSWORD=admin \
  -e MONGO_INITDB_DATABASE=biostudies-dev \
  mongo:4.4;
