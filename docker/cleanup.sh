#!/bin/bash

# Docker Cleanup Script for Bookcase Java Application
# This script removes containers, images, volumes, and networks related to the project

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

echo -e "${BLUE}🧹 Docker Cleanup Script for ${PROJECT_NAME}${NC}"
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

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    print_error "Docker is not running. Please start Docker first."
    exit 1
fi

echo -e "${YELLOW}This will remove ALL Docker resources for ${PROJECT_NAME}${NC}"
echo "Including:"
echo "  - Containers (running and stopped)"
echo "  - Images (built and downloaded)"
echo "  - Volumes (database data will be lost!)"
echo "  - Networks"
echo "  - Build cache"
echo

# Confirmation prompt
read -p "Are you sure you want to continue? (y/N): " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "Cleanup cancelled."
    exit 0
fi

echo
echo "Starting cleanup process..."

# 1. Stop and remove containers using docker-compose
echo -e "${BLUE}🛑 Stopping and removing containers...${NC}"
if [ -f "$COMPOSE_FILE" ]; then
    docker-compose -f "$COMPOSE_FILE" down --remove-orphans 2>/dev/null || true
    print_status "Containers stopped and removed"
else
    print_warning "Docker compose file not found, skipping compose down"
fi

# 2. Remove any remaining project containers
echo -e "${BLUE}🗑️ Removing any remaining project containers...${NC}"
PROJECT_CONTAINERS=$(docker ps -aq --filter "name=${PROJECT_NAME}" 2>/dev/null || true)
if [ ! -z "$PROJECT_CONTAINERS" ]; then
    docker rm -f $PROJECT_CONTAINERS 2>/dev/null || true
    print_status "Additional project containers removed"
else
    print_status "No additional containers to remove"
fi

# 3. Remove project images
echo -e "${BLUE}🖼️ Removing project images...${NC}"
PROJECT_IMAGES=$(docker images --filter "reference=${PROJECT_NAME}*" -q 2>/dev/null || true)
if [ ! -z "$PROJECT_IMAGES" ]; then
    docker rmi -f $PROJECT_IMAGES 2>/dev/null || true
    print_status "Project images removed"
fi

# Remove related images
RELATED_IMAGES=$(docker images --filter "reference=*${PROJECT_NAME}*" -q 2>/dev/null || true)
if [ ! -z "$RELATED_IMAGES" ]; then
    docker rmi -f $RELATED_IMAGES 2>/dev/null || true
    print_status "Related images removed"
fi

# 4. Remove volumes
echo -e "${BLUE}💾 Removing project volumes...${NC}"
PROJECT_VOLUMES=$(docker volume ls --filter "name=${PROJECT_NAME}" -q 2>/dev/null || true)
if [ ! -z "$PROJECT_VOLUMES" ]; then
    docker volume rm $PROJECT_VOLUMES 2>/dev/null || true
    print_status "Project volumes removed"
fi

# Remove bookcase_data volume specifically
if docker volume ls | grep -q "bookcase_data"; then
    docker volume rm bookcase_data 2>/dev/null || true
    print_status "Bookcase data volume removed"
fi

# 5. Remove networks
echo -e "${BLUE}🌐 Removing project networks...${NC}"
PROJECT_NETWORKS=$(docker network ls --filter "name=${PROJECT_NAME}" --format "{{.Name}}" 2>/dev/null || true)
if [ ! -z "$PROJECT_NETWORKS" ]; then
    echo "$PROJECT_NETWORKS" | xargs -r docker network rm 2>/dev/null || true
    print_status "Project networks removed"
fi

# Remove bookcase-network specifically
if docker network ls | grep -q "bookcase-network"; then
    docker network rm bookcase-network 2>/dev/null || true
    print_status "Bookcase network removed"
fi

# 6. Clean up dangling images and build cache
echo -e "${BLUE}🧽 Cleaning up dangling resources...${NC}"
docker image prune -f > /dev/null 2>&1 || true
docker builder prune -f > /dev/null 2>&1 || true
print_status "Dangling images and build cache cleaned"

# 7. Optional: Full system cleanup
echo
read -p "Do you want to run a full Docker system cleanup? This will remove ALL unused Docker resources (y/N): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo -e "${BLUE}🔥 Running full system cleanup...${NC}"
    docker system prune -af --volumes > /dev/null 2>&1 || true
    print_status "Full system cleanup completed"
fi

# 8. Show final status
echo
echo -e "${GREEN}🎉 Cleanup completed successfully!${NC}"
echo
echo "Summary of cleaned resources:"
echo "  ✅ Project containers removed"
echo "  ✅ Project images removed" 
echo "  ✅ Project volumes removed (data lost)"
echo "  ✅ Project networks removed"
echo "  ✅ Build cache cleared"
echo
echo "You can now run './docker/start.sh' to start fresh."
