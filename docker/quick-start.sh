#!/bin/bash

# Quick Start - Build app and start containers in one command
set -e

echo "🚀 Quick Start: Building app and starting containers..."

# Build the application
echo "Building application..."
./mvnw clean package -DskipTests

# Start Docker services
echo "Starting Docker services..."
./docker/start.sh
