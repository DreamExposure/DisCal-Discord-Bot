version=$1

podman build --events-backend=file -t rg.nl-ams.scw.cloud/dreamexposure/discal-server:"$version" --file ./Dockerfile .

podman push --events-backend=file rg.nl-ams.scw.cloud/dreamexposure/discal-server:"$version" --creds="$SCW_USER:$SCW_SECRET"
