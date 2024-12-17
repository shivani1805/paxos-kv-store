#!/bin/bash

NETWORK_NAME="my-network"
SERVER_IMAGE="test-server-image"
CLIENT_IMAGE="test-client-image-1"
SERVER_CONTAINER_NAME="test-server-container"
CLIENT_CONTAINER_PREFIX="test-client-container-1"
SERVER_PORT=5002
LOCAL_PORT=1009
SERVER_NAME="test-server"
SERVER_REPLICAS_START=5002

# Create the Docker network if it doesn't exist
docker network inspect $NETWORK_NAME >/dev/null 2>&1 || docker network create $NETWORK_NAME
echo "Docker network '$NETWORK_NAME' created."

# Build the server Docker image
docker build -t $SERVER_IMAGE -f Dockerfile.server .
echo "Server image '$SERVER_IMAGE' built."

# Run the server container
docker run -d --network $NETWORK_NAME --name $SERVER_CONTAINER_NAME -p $LOCAL_PORT:$SERVER_PORT $SERVER_IMAGE $SERVER_PORT $SERVER_NAME
echo "Server container '$SERVER_CONTAINER_NAME' started."

# Retrieve the server container's IP address
SERVER_IP=$(docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' $SERVER_CONTAINER_NAME)
echo "Server IP retrieved: $SERVER_IP"

# Build the client Docker image
docker build -t $CLIENT_IMAGE -f Dockerfile.client .
echo "Client image '$CLIENT_IMAGE' built."

# Run the first client container in interactive mode
echo "Starting the first client container interactively..."
docker run -it --network $NETWORK_NAME --name ${CLIENT_CONTAINER_PREFIX} $CLIENT_IMAGE $SERVER_IP $SERVER_REPLICAS_START $SERVER_NAME client-1

