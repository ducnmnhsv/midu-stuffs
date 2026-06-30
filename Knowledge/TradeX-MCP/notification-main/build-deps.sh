#!/bin/bash

# Build dependencies script for Notification Service
# This script builds the tradex-common-java dependency first, then the notification service

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

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Get the parent directory (NHSV-source)
PARENT_DIR="$(cd .. && pwd)"
COMMON_PROJECT_DIR="$PARENT_DIR/tradex-common-java"
NOTIFICATION_PROJECT_DIR="$(pwd)"

print_status "Starting dependency build process..."
print_status "Parent directory: $PARENT_DIR"
print_status "Common project directory: $COMMON_PROJECT_DIR"
print_status "Notification project directory: $NOTIFICATION_PROJECT_DIR"

# Check if tradex-common-java exists
if [ ! -d "$COMMON_PROJECT_DIR" ]; then
    print_error "tradex-common-java directory not found at $COMMON_PROJECT_DIR"
    exit 1
fi

# Check if Maven is available
if ! command -v mvn &> /dev/null; then
    print_error "Maven is not installed or not in PATH"
    print_status "Please install Maven or ensure it's in your PATH"
    exit 1
fi

# Step 1: Build tradex-common-java
print_status "Step 1: Building tradex-common-java dependency..."
cd "$COMMON_PROJECT_DIR"

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
mvn clean compile -DskipTests

if [ $? -eq 0 ]; then
    print_success "Notification service compiled successfully"
else
    print_error "Failed to compile notification service"
    exit 1
fi

print_success "Dependency build process completed successfully!"
print_status "Both projects are now built and ready for development."
