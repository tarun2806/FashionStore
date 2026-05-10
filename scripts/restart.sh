#!/bin/bash

set -e

echo "Restarting FashionStore Docker services..."

docker compose restart

echo "Services restarted successfully!"
echo "Run 'docker compose logs -f' to view logs."
