#!/bin/bash

set -e

echo "Stopping FashionStore Docker services..."

docker compose down

echo "Services stopped successfully!"
echo "To remove volumes as well, run: docker compose down -v"
