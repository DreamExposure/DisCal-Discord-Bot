version=$1

podman build -t rg.nl-ams.scw.cloud/dreamexposure/discal-web:"$version" --file ./Dockerfile .

podman push rg.nl-ams.scw.cloud/dreamexposure/discal-web:"$version" --creds="$SCW_USER:$SCW_SECRET"
