#!/bin/bash

version=$1
echo "Version: $version"

case "$version" in
  *SNAPSHOT*)
    version="latest"
    ;;
esac

echo "Using image tag: $version"

podman build --events-backend=file -t rg.nl-ams.scw.cloud/dreamexposure/discal-server:"$version" --file \
./Dockerfile .

podman push --events-backend=file rg.nl-ams.scw.cloud/dreamexposure/discal-server:"$version" \
--creds="$SCW_USER:$SCW_SECRET"
