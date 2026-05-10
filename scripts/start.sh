#!/bin/bash

set -e

echo "Starting FashionStore Docker services..."

# Check if .env file exists
if [ ! -f .env ]; then
    echo "Error: .env file not found. Please copy .env.example to .env and configure it."
    exit 1
fi

# Start services
docker compose up -d

echo "Services started successfully!"
echo "Application will be available at:"
echo "  - Main site: http://localhost"
echo "  - Admin panel: http://localhost/admin"
echo "  - Backend API: http://localhost/api"
echo ""
echo "Run 'docker compose logs -f' to view logs."
