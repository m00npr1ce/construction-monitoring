@echo off
echo Starting Construction Monitoring System...

echo.
echo 1. Starting PostgreSQL with Docker...
docker-compose -f docker-compose.dev.yml up -d

echo.
echo 2. Waiting for PostgreSQL to be ready...
timeout /t 10 /nobreak > nul

echo.
echo 3. Starting Backend...
start "Backend" cmd /k "cd backend && mvn spring-boot:run"

echo.
echo 4. Waiting for Backend to start...
timeout /t 15 /nobreak > nul

echo.
echo 5. Starting Frontend...
start "Frontend" cmd /k "cd frontend && npm run dev"

echo.
echo System is starting up...
echo.
echo Services will be available at:
echo - Frontend: http://localhost:5173
echo - Backend API: http://localhost:8080
echo - PostgreSQL: localhost:5432
echo.
echo Press any key to stop all services...
pause > nul

echo.
echo Stopping services...
docker-compose -f docker-compose.dev.yml down
taskkill /f /im java.exe 2>nul
taskkill /f /im node.exe 2>nul
echo All services stopped.


