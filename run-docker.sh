#!/bin/bash

# Simple script to run Robot Wars Battle Engine with Docker
# Run from robots/ folder: ./run-docker.sh

set -e

echo "🤖 Robot Wars Battle Engine - Docker Runner"
echo "============================================"
echo ""

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo "❌ Docker is not installed. Please install Docker first."
    exit 1
fi

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker and try again."
    exit 1
fi

# Setup X11 forwarding for Linux
echo "🐧 Setting up X11 forwarding..."
xhost +local:docker 2>/dev/null || echo "⚠️  Note: X11 setup may require manual configuration"

echo ""
echo "🔨 Building Docker image (this may take a few minutes the first time)..."
echo ""

# Build from parent directory to access api/ and libs/
cd "$(dirname "$0")/.."
docker build -f robots/Dockerfile -t robot-wars . > /dev/null 2>&1 || {
    echo "Building with verbose output..."
    docker build -f robots/Dockerfile -t robot-wars .
}

echo ""
echo "🚀 Starting Robot Wars Battle Engine..."
echo ""

# Run the container
docker run -it --rm \
    -e DISPLAY=$DISPLAY \
    -v /tmp/.X11-unix:/tmp/.X11-unix:rw \
    --network host \
    robot-wars

echo ""
echo "✅ Application closed."
