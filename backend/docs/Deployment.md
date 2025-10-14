# Руководство по развертыванию (тестовый сервер)

## Предпосылки
- Ubuntu 22.04+ (или Windows Server с Docker Desktop)
- Docker, Docker Compose
- Доступ по SSH

## Шаги
1) Клонировать репозиторий:
```bash
git clone <repo_url>
cd construction-monitoring
```

2) Запуск в dev‑режиме (hot reload):
```bash
docker-compose -f docker-compose.dev.yml up -d --build
```
- Backend: http://<host>:8080
- Frontend (Vite): http://<host>:5173

3) Продовый режим (вариант):
- Собрать образы frontend/backend (Dockerfile), Nginx как reverse proxy для фронтенда и проксирование /api на backend.
- Пример Nginx (файл /etc/nginx/sites-available/app):
```
server {
    listen 80;
    server_name _;

    location / {
        root /usr/share/nginx/html;
        try_files $uri /index.html;
    }

    location /api/ {
        proxy_pass http://backend:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
```
- В docker‑compose добавить сервис nginx и volume со сборкой фронтенда.

4) Переменные окружения
- См. `docker-compose.dev.yml` (SPRING_DATASOURCE_URL, JWT_SECRET, VITE_API_BASE).

5) Здоровье/мониторинг
- Проверки: `GET /api/test/me` c JWT; логи `docker-compose logs -f backend`.
- Для production — Prometheus/Grafana или внешние APM.

6) Бекапы БД
- См. `Backup.md`.

## Приёмка
- Пройти чек‑лист из SRS и TestPlan; запустить k6 тест `k6 run backend/docs/k6-load-test.js`.
