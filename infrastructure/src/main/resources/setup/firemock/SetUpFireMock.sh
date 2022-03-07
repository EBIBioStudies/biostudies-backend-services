#!/usr/bin/env bash
docker run -d \
  --name biostudies-firemock \
  -p 8092:8080 \
  dockerhub.ebi.ac.uk/bioimage-archive/fire-mock:latest;
