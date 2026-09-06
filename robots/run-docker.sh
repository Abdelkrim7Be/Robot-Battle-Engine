#!/usr/bin/env bash

set -e

echo "Robot Wars Battle Engine"
echo ""

if ! command -v docker &> /dev/null; then
    echo "Docker is not installed."
    exit 1
fi

if ! docker info > /dev/null 2>&1; then
    echo "Docker is not running."
    exit 1
fi

if command -v xhost > /dev/null 2>&1; then
    xhost +local:docker > /dev/null 2>&1 || true
fi

echo ""
echo "Building Docker image..."
echo ""

cd "$(dirname "$0")/.."
docker build -f robots/Dockerfile -t robot-wars . > /dev/null 2>&1 || {
    echo "Building with verbose output..."
    docker build -f robots/Dockerfile -t robot-wars .
}

echo ""
echo "Starting application..."
echo ""

docker run -it --rm \
    -e DISPLAY="${DISPLAY:-:0}" \
    -v /tmp/.X11-unix:/tmp/.X11-unix:rw \
    --network host \
    robot-wars

echo ""
echo "Application closed."
