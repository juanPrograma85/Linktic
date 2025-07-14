#!/bin/bash

echo "Starting Microservices with Docker Compose (using sudo)..."
echo ""

if [ ! -d "../ProductoService" ]; then
    echo "ProductoService directory not found at ../ProductoService"
    exit 1
fi

if [ ! -f "../ProductoService/Dockerfile" ]; then
    echo "ProductoService Dockerfile not found!"
    exit 1
fi

echo "Stopping existing containers..."
sudo docker compose down

echo "Building and starting services..."
sudo docker compose up --build -d

if [ $? -eq 0 ]; then
    echo ""
    echo "Services started successfully!"
    echo ""
    echo "Service Status:"
    sudo docker compose ps
    
    echo ""
    echo "Waiting for services to be ready..."
    sleep 15
    
    echo ""
    echo "Service URLs:"
    echo "  - Producto Service:    http://localhost:8080"
    echo "  - Producto Swagger:    http://localhost:8080/swagger-ui/index.html"
    echo "  - Inventario Service:  http://localhost:8081" 
    echo "  - Inventario Swagger:  http://localhost:8081/swagger-ui/index.html"
    echo "  - Inventario Health:   http://localhost:8081/health"
    echo ""
    echo "Database Consoles:"
    echo "  - Producto H2:         http://localhost:8080/h2-console"
    echo "    JDBC URL: jdbc:h2:file:/app/data/productosdb"
    echo "  - Inventario H2:       http://localhost:8081/h2-console"
    echo "    JDBC URL: jdbc:h2:file:/app/data/inventariodb"
    echo ""
    echo "Useful commands:"
    echo "  - View logs:           sudo docker compose logs -f [service-name]"
    echo "  - Stop services:       sudo docker compose down"
    echo "  - Restart service:     sudo docker compose restart [service-name]"
    echo ""
    echo "Test communication:"
    echo "  ./test-communication.sh"
else
    echo "Failed to start services. Check the logs:"
    sudo docker compose logs
    exit 1
fi
