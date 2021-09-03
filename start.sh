#!/bin/sh


NAME="flux-application-service"
IMAGE="flux-application-service"
CONTAINER=flux-application-service
PORT=8080

mvn clean package

docker kill ${NAME} || echo "Nothing to be stopped, continuing"
docker rm ${NAME} || echo "Nothing to be removed, continuing"

docker image build --network=host --no-cache -t ${IMAGE} .

docker run -d -p=${PORT}:${PORT} -h ${CONTAINER} --name ${CONTAINER} \
 -e SPRING_PROFILES_ACTIVE=local \
 -e KINESIS_STREAM_ENABLED="true" \
   ${IMAGE}
