#!/bin/bash

# JHipster eKyc Admin - Docker Build from Parent Directory
# This script builds the Docker image from the parent directory to access both projects

set -e

echo "=========================================="
echo "JHipster eKyc Admin - Docker Build from Parent"
echo "=========================================="
echo ""

# Check if we're in the right directory
if [ ! -f "pom.xml" ]; then
    echo "❌ Error: This script must be run from the ekyc-admin directory"
    echo "Current directory: $(pwd)"
    exit 1
fi

# Check if tradex-common-java exists
if [ ! -d "../tradex-common-java" ]; then
    echo "❌ Error: tradex-common-java project not found in parent directory"
    echo "Expected location: ../tradex-common-java"
    exit 1
fi

echo "🔍 Found tradex-common-java project at: ../tradex-common-java"
echo ""

# Check if Docker is available
if ! command -v docker &> /dev/null; then
    echo "❌ Docker is not installed or not in PATH"
    echo "Please install Docker first: https://docs.docker.com/get-docker/"
    exit 1
fi

echo "🐳 Building Docker image from parent directory context..."
echo "This will:"
echo "1. Build tradex-common first"
echo "2. Build ekyc-admin with all dependencies"
echo "3. Create production Docker image"
echo ""

# Change to parent directory for Docker build context
cd ..

echo "Building from: $(pwd)"
echo "Projects available:"
ls -la | grep -E "(ekyc-admin|tradex-common-java)"
echo ""

# Build the Docker image from parent directory
echo "Building Docker image..."
docker build -f ekyc-admin/dockerfile -t ekyc-admin:latest .

if [ $? -eq 0 ]; then
    echo ""
    echo "🎉 Success! Docker image built with all dependencies!"
    echo ""
    echo "To run the container:"
    echo "  docker run -p 8080:8080 ekyc-admin:latest"
    echo ""
    echo "To run with Docker Compose (from ekyc-admin directory):"
    echo "  cd ekyc-admin && docker-compose up -d"
else
    echo ""
    echo "❌ Docker build failed!"
    exit 1
fi

# Return to ekyc-admin directory
cd ekyc-admin
