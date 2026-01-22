#!/usr/bin/env bash

TOKEN="eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ7XCJpZFwiOjMsXCJlbWFpbFwiOlwiYmlvc3R1ZGllcy1kZXZAZWJpLmFjLnVrXCIsXCJmdWxsTmFtZVwiOlwiQmlvU3R1ZGllcyBtYW5hZ2VyXCIsXCJjcmVhdGlvblRpbWVcIjoxNjQyMzkyOTI0fSJ9.YtyoQzpd2-fkDTwEdVZa1JrD2LfK3v7G9durVZ671kWbP494Xl0VVDqD0qOXy-AW-CipsOlhZH4t6yTlgjaidw"

BASE_URL="http://biostudies-prod.ebi.ac.uk:8788/security/users"

emails=(
)

for email in "${emails[@]}"; do
  curl --silent --output /dev/null --show-error --fail \
    --location "${BASE_URL}/${email}/migrate" \
    --header "X-Session-Token: ${TOKEN}" \
    --header "Content-Type: application/json" \
    --data '{"storageMode":"FTP"}' \
    || true
done

