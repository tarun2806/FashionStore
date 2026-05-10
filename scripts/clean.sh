#!/bin/bash

set -e

echo "Cleaning FashionStore Docker environment..."

echo "Stopping and removing containers..."
docker compose down -v

echo "Removing images..."
docker compose down --rmi all

echo "Removing volumes..."
docker volume rm fashionstore-mysql-data fashionstore-redis-data 2>/dev/null || true

echo "Removing network..."
docker network rm fashionstore-network 2>/dev/null || true

echo "Clean complete!"
