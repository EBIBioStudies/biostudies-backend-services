#!/usr/bin/env bash
echo "holi Dockerfile.$1";
docker build -t biostudies-mysql:0.2 -f Dockerfile.$1 .;
docker run -d \
  --name biostudies-mysql \
  -e MYSQL_ROOT_PASSWORD=admin \
  -e MYSQL_DATABASE=BioStudiesDev \
  -p 3306:3306 \
  biostudies-mysql:0.2 \
  --lower-case-table-names=1;
