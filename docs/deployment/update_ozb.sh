#!/bin/bash

# This script is created to update the OZB application

if [ -z $1 ]; then
        echo "No version is provided 'latest' tag will be used for the docker image."
fi

# log file
ofile="update_ozb_history/update_ozb_"$(date "+%Y.%m.%d-%H.%M.%S")".log"

# container name
container="OpenZaakBrug"

# docker hub registry(repository)
repo="openzaakbrug/openzaakbrug"
tag=${1:-latest}

# create the log file
touch $ofile

# stop the docker container
echo "Attempting to stop the container ["$container"]" >> $ofile
docker stop $container >> $ofile
# remove docker container
echo "Attempting to remove the container ["$container"]" >> $ofile
docker rm $container >> $ofile

# remove any old ozb image from local env.
echo "Removing old images from the repo = "$repo >> $ofile
docker rmi $(docker images $repo -aq) >> $ofile

# pull latest docker image from docker hub
echo "Pulling the latest image from docker hub repo ["$repo"]" >> $ofile
docker pull $repo:$tag >> $ofile

# runs the image with provided version in a container named OpenZaakBrug
# the network needs to be the host network to resolve db's ip address or hostname(for some reason it does not find the ip address) investigate later
# the image will be restarted unless it is stopped. Therefore after running this script make sure that the application is running without any error
# Otherwise, it will keep restarting and fail with the same error resulting in an infinite loop.
# For ladybug and the database the container uses two volumes
# Remark: since the application uses 8080 port in the application it will also use 8080 in the host machine. No need to specify it expilicitly.(see network="host")
echo "Starting the application in container ["$container"]" >> $ofile
docker run --network="host" --name $container -e JAVA_TOOL_OPTIONS="-Xms150M -Xmx4G -verbose:gc" --restart unless-stopped -d -v /root/config:/home/config -v data-debug:/home/data $repo:$tag >> $ofile

