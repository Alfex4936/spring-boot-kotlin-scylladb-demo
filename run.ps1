# This is the equivalent of '@echo off' in batch
$ErrorActionPreference = "SilentlyContinue"

# This runs './gradlew bootJar' command
./gradlew clean bootJar build -x test

copy Dockerfile.spring build/libs/
docker-compose -f scylla.yml up --build -d
