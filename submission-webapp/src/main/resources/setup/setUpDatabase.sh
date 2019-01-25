#!/usr/bin/env bash
docker build -t biostudies-mysql:0.2 .;
docker run -d \
  --name biostudies-mysql \
  -e MYSQL_ROOT_PASSWORD=admin \
  -e MYSQL_DATABASE=BioStudiesDev \
  -p 3306:3306 \
  biostudies-mysql;
