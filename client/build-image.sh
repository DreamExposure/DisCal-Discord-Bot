#!/bin/bash

version=$1

if [[ $version == *"SNAPSHOT"* ]]; then
  version="latest"
fi

echo "Using image tag: $version"

podman build --events-backend=file -t rg.nl-ams.scw.cloud/dreamexposure/discal-client:"$version" --file \
./Dockerfile .

podman push --events-backend=file rg.nl-ams.scw.cloud/dreamexposure/discal-client:"$version" \
--creds="$SCW_USER:$SCW_SECRET"
