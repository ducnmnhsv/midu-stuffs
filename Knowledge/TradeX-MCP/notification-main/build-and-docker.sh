#!/bin/bash

# Build and Docker script for Notification Service
# This script builds the tradex-common-java dependency first, then the notification service,
# and finally creates the Docker image.

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Get the parent directory (NHSV-source)
PARENT_DIR="$(cd .. && pwd)"
COMMON_PROJECT_DIR="$PARENT_DIR/tradex-common-java"
NOTIFICATION_PROJECT_DIR="$(pwd)"

print_status "Starting build process..."
print_status "Parent directory: $PARENT_DIR"
print_status "Common project directory: $COMMON_PROJECT_DIR"
print_status "Notification project directory: $NOTIFICATION_PROJECT_DIR"

# Check if tradex-common-java exists
if [ ! -d "$COMMON_PROJECT_DIR" ]; then
    print_error "tradex-common-java directory not found at $COMMON_PROJECT_DIR"
    exit 1
fi

# Check if pom.xml exists in tradex-common-java
if [ ! -f "$COMMON_PROJECT_DIR/pom.xml" ]; then
    print_error "pom.xml not found in tradex-common-java directory"
    exit 1
fi

# Step 1: Build tradex-common-java
print_status "Step 1: Building tradex-common-java dependency..."
cd "$COMMON_PROJECT_DIR"

# Check if Maven is available
if ! command -v mvn &> /dev/null; then
    print_error "Maven is not installed or not in PATH"
    print_status "Please install Maven or ensure it's in your PATH"
    exit 1
fi

print_status "Building tradex-common-java with Maven..."
mvn clean install -DskipTests

if [ $? -eq 0 ]; then
    print_success "tradex-common-java built successfully"
else
    print_error "Failed to build tradex-common-java"
    exit 1
fi

# Step 2: Build notification service
print_status "Step 2: Building notification service..."
cd "$NOTIFICATION_PROJECT_DIR"

print_status "Building notification service with Maven..."
mvn clean package -DskipTests

if [ $? -eq 0 ]; then
    print_success "Notification service built successfully"
else
    print_error "Failed to build notification service"
    exit 1
fi

# Step 3: Build Docker image
print_status "Step 3: Building Docker image..."

# Check if Docker is available
if ! command -v docker &> /dev/null; then
    print_error "Docker is not installed or not in PATH"
    print_status "Please install Docker or ensure it's in your PATH"
    exit 1
fi

# Check if the JAR file was created
JAR_FILE="target/notification-1.0-SNAPSHOT.jar"
if [ ! -f "$JAR_FILE" ]; then
    print_error "JAR file not found at $JAR_FILE"
    print_status "Please check the build output for errors"
    exit 1
fi

print_status "Building Docker image..."
docker build -t notification-service:latest .

if [ $? -eq 0 ]; then
    print_success "Docker image built successfully!"
    print_status "Image tag: notification-service:latest"
    
    # Show image info
    print_status "Docker image details:"
    docker images notification-service:latest
    
    print_status "You can now run the container with:"
    print_status "docker run -d --name notification-service -p 8080:8080 notification-service:latest"
else
    print_error "Failed to build Docker image"
    exit 1
fi

print_success "Build process completed successfully!"
print_status "All components built and Docker image created."
