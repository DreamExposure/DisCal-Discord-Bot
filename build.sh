#!/bin/bash

mvn clean package

# build docker images
docker build -t rg.nl-ams.scw.cloud/dreamexposure/discal-server:latest --file server/Dockerfile ./server

docker build -t rg.nl-ams.scw.cloud/dreamexposure/discal-client:latest --file client/Dockerfile ./client

docker build -t rg.nl-ams.scw.cloud/dreamexposure/discal-web:latest --file web/Dockerfile ./web

# deploy docker images
docker push rg.nl-ams.scw.cloud/dreamexposure/discal-server:latest
docker push rg.nl-ams.scw.cloud/dreamexposure/discal-client:latest
docker push rg.nl-ams.scw.cloud/dreamexposure/discal-web:latest
