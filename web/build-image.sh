#!/bin/bash

version=$1

if [[ $version == *"SNAPSHOT"* ]]; then
  version="latest"
fi

echo "Using image tag: $version"

podman build --events-backend=file -t rg.nl-ams.scw.cloud/dreamexposure/discal-web:"$version" --file \
./Dockerfile .

podman push --events-backend=file rg.nl-ams.scw.cloud/dreamexposure/discal-web:"$version" \
--creds="$SCW_USER:$SCW_SECRET"
