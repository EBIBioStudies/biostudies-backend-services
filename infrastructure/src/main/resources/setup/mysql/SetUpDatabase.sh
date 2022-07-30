#!/usr/bin/env bash
docker build -t biostudies-mysql:1.3 -f Dockerfile.$1 .;
docker run -d \
  --name biostudies-mysql \
  -e MYSQL_ROOT_PASSWORD=admin \
  -e MYSQL_DATABASE=BioStudiesDev \
  -p 3306:3306 \
  biostudies-mysql:1.2 \
  --lower-case-table-names=1;
