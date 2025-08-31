#!/bin/bash

# Docker Startup Script for Bookcase Java Application
# This script builds and starts the application with proper error handling

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Project configuration
PROJECT_NAME="bookcase-java"
COMPOSE_FILE="docker/docker-compose.yml"
JAR_FILE="target/*.jar"

# Detect Docker Compose command
if command -v docker-compose &> /dev/null; then
    DOCKER_COMPOSE="docker-compose"
elif docker compose version &> /dev/null; then
    DOCKER_COMPOSE="docker compose"
else
    echo -e "${RED}❌ Docker Compose not found. Please install Docker Compose.${NC}"
    exit 1
fi

echo -e "${BLUE}🚀 Docker Startup Script for ${PROJECT_NAME}${NC}"
echo "Using: $DOCKER_COMPOSE"
echo "========================================"

# Function to print status
print_status() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️ $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ️ $1${NC}"
}

# Function to check if port is in use
check_port() {
    local port=$1
    if command -v lsof &> /dev/null; then
        if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
            return 0  # Port is in use
        else
            return 1  # Port is free
        fi
    elif command -v netstat &> /dev/null; then
        if netstat -tuln | grep -q ":$port "; then
            return 0  # Port is in use
        else
            return 1  # Port is free
        fi
    else
        # Skip port check if no tools available
        return 1
    fi
}

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    print_error "Docker is not running. Please start Docker first."
    exit 1
fi

# Check if compose file exists
if [ ! -f "$COMPOSE_FILE" ]; then
    print_error "Docker compose file not found: $COMPOSE_FILE"
    exit 1
fi

# Check if JAR file exists
if ! ls $JAR_FILE 1> /dev/null 2>&1; then
    print_warning "JAR file not found. Building application first..."
    
    echo -e "${BLUE}🔨 Building application with Maven...${NC}"
    ./mvnw clean package -DskipTests
    
    if [ $? -eq 0 ]; then
        print_status "Application built successfully"
    else
        print_error "Failed to build application"
        exit 1
    fi
fi

# Check if ports are available
echo -e "${BLUE}🔍 Checking port availability...${NC}"
if check_port 9000; then
    print_warning "Port 9000 is already in use. The application might conflict."
    echo "You can stop existing services with: $DOCKER_COMPOSE -f $COMPOSE_FILE down"
fi

if check_port 9001; then
    print_warning "Port 9001 is already in use. phpMyAdmin might conflict."
fi

# Stop any existing containers
echo -e "${BLUE}🛑 Stopping existing containers...${NC}"
$DOCKER_COMPOSE -f "$COMPOSE_FILE" down 2>/dev/null || true
print_status "Existing containers stopped"

# Pull latest base images
echo -e "${BLUE}📥 Pulling latest base images...${NC}"
$DOCKER_COMPOSE -f "$COMPOSE_FILE" pull db phpmyadmin 2>/dev/null || true
print_status "Base images updated"

# Build and start services
echo -e "${BLUE}🔧 Building and starting services...${NC}"
$DOCKER_COMPOSE -f "$COMPOSE_FILE" up --build -d

if [ $? -eq 0 ]; then
    print_status "Services started successfully"
else
    print_error "Failed to start services"
    echo
    echo "Checking logs for errors:"
    $DOCKER_COMPOSE -f "$COMPOSE_FILE" logs --tail=20
    exit 1
fi

# Wait for services to be healthy
echo -e "${BLUE}⏳ Waiting for services to be healthy...${NC}"
echo "This may take up to 60 seconds..."

# Function to wait for service health
wait_for_service() {
    local service=$1
    local max_attempts=30
    local attempt=1
    
    while [ $attempt -le $max_attempts ]; do
        if $DOCKER_COMPOSE -f "$COMPOSE_FILE" ps "$service" | grep -q "healthy\|Up"; then
            return 0
        fi
        echo -n "."
        sleep 2
        attempt=$((attempt + 1))
    done
    return 1
}

# Wait for database
echo -n "Waiting for database"
if wait_for_service "db"; then
    echo
    print_status "Database is healthy"
else
    echo
    print_error "Database failed to start properly"
    $DOCKER_COMPOSE -f "$COMPOSE_FILE" logs db --tail=10
    exit 1
fi

# Wait for application
echo -n "Waiting for application"
if wait_for_service "app"; then
    echo
    print_status "Application is running"
else
    echo
    print_warning "Application may still be starting. Check logs if needed."
fi

# Show service status
echo
echo -e "${BLUE}📊 Service Status:${NC}"
$DOCKER_COMPOSE -f "$COMPOSE_FILE" ps

echo
echo -e "${GREEN}🎉 Startup completed!${NC}"
echo
echo "Services are available at:"
echo -e "  📱 Application: ${GREEN}http://localhost:9000${NC}"
echo -e "  🗄️ phpMyAdmin: ${GREEN}http://localhost:9001${NC}"
echo "     Username: spring"
echo "     Password: ThePassword"
echo
echo "Useful commands:"
echo "  📋 View logs:     $DOCKER_COMPOSE -f $COMPOSE_FILE logs -f"
echo "  🔄 Restart:       $DOCKER_COMPOSE -f $COMPOSE_FILE restart"
echo "  🛑 Stop:          $DOCKER_COMPOSE -f $COMPOSE_FILE down"
echo "  🧹 Full cleanup:  ./docker/cleanup.sh"
echo
print_info "Application logs will be shown below. Press Ctrl+C to stop following logs."
echo

# Follow application logs
$DOCKER_COMPOSE -f "$COMPOSE_FILE" logs -f app
