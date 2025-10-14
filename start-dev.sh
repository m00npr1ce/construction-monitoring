#!/bin/bash

echo "Starting Construction Monitoring System..."

echo ""
echo "1. Starting PostgreSQL with Docker..."
docker-compose -f docker-compose.dev.yml up -d

echo ""
echo "2. Waiting for PostgreSQL to be ready..."
sleep 10

echo ""
echo "3. Starting Backend..."
cd backend && mvn spring-boot:run &
BACKEND_PID=$!

echo ""
echo "4. Waiting for Backend to start..."
sleep 15

echo ""
echo "5. Starting Frontend..."
cd ../frontend && npm run dev &
FRONTEND_PID=$!

echo ""
echo "System is starting up..."
echo ""
echo "Services will be available at:"
echo "- Frontend: http://localhost:5173"
echo "- Backend API: http://localhost:8080"
echo "- PostgreSQL: localhost:5432"
echo ""
echo "Press Ctrl+C to stop all services..."

# Функция для остановки всех сервисов
cleanup() {
    echo ""
    echo "Stopping services..."
    docker-compose -f docker-compose.dev.yml down
    kill $BACKEND_PID 2>/dev/null
    kill $FRONTEND_PID 2>/dev/null
    echo "All services stopped."
    exit 0
}

# Перехватываем Ctrl+C
trap cleanup SIGINT

# Ждем завершения
wait


