#!/bin/bash

version=$1
versionName=$version

if [[ $version == *"SNAPSHOT"* ]]; then
  versionName="latest"
fi

podman build --events-backend=file -t rg.nl-ams.scw.cloud/dreamexposure/discal-web:"$versionName" --file \
./Dockerfile .

podman push --events-backend=file rg.nl-ams.scw.cloud/dreamexposure/discal-web:"$versionName" \
--creds="$SCW_USER:$SCW_SECRET"
