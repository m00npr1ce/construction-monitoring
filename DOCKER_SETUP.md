# Docker Setup для Construction Monitoring System

## Быстрый запуск

### 1. Клонирование и подготовка
```bash
git clone <repository-url>
cd construction-monitoring
```

### 2. Запуск в режиме разработки
```bash
# Запуск всех сервисов
docker-compose up -d

# Просмотр логов
docker-compose logs -f

# Остановка
docker-compose down
```

### 3. Запуск в продакшене
```bash
# Создайте .env файл с настройками
cp env.example .env
# Отредактируйте .env файл с вашими настройками

# Запуск продакшен версии
docker-compose -f docker-compose.prod.yml up -d
```

## Доступ к приложению

После запуска система будет доступна по адресам:
- **Frontend**: http://localhost
- **Backend API**: http://localhost:8080
- **PostgreSQL**: localhost:5432

## Управление контейнерами

### Основные команды
```bash
# Запуск
docker-compose up -d

# Остановка
docker-compose down

# Перезапуск
docker-compose restart

# Просмотр статуса
docker-compose ps

# Просмотр логов
docker-compose logs [service_name]

# Вход в контейнер
docker-compose exec [service_name] sh
```

### Управление данными
```bash
# Создание резервной копии БД
docker-compose exec postgres pg_dump -U construction_user construction_monitoring > backup.sql

# Восстановление БД
docker-compose exec -T postgres psql -U construction_user construction_monitoring < backup.sql

# Очистка всех данных (ОСТОРОЖНО!)
docker-compose down -v
```

## Мониторинг и логи

### Просмотр логов
```bash
# Все сервисы
docker-compose logs -f

# Конкретный сервис
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f postgres
```

### Мониторинг ресурсов
```bash
# Использование ресурсов
docker stats

# Информация о контейнерах
docker-compose ps
```

## Обновление системы

### Обновление кода
```bash
# Остановка сервисов
docker-compose down

# Обновление кода
git pull origin main

# Пересборка и запуск
docker-compose up -d --build
```

### Обновление только backend
```bash
docker-compose up -d --build backend
```

### Обновление только frontend
```bash
docker-compose up -d --build frontend
```

## Устранение неполадок

### Проблемы с запуском
```bash
# Проверка статуса
docker-compose ps

# Просмотр логов ошибок
docker-compose logs backend
docker-compose logs postgres

# Перезапуск проблемного сервиса
docker-compose restart backend
```

### Проблемы с базой данных
```bash
# Проверка подключения к БД
docker-compose exec postgres psql -U construction_user -d construction_monitoring -c "SELECT 1;"

# Сброс БД (ОСТОРОЖНО!)
docker-compose down
docker volume rm construction-monitoring_postgres_data
docker-compose up -d
```

### Проблемы с файлами
```bash
# Проверка загруженных файлов
docker-compose exec backend ls -la /app/uploads

# Очистка файлов
docker-compose exec backend rm -rf /app/uploads/*
```

## Безопасность

### Настройка переменных окружения
```bash
# Создайте .env файл
cp env.example .env

# Отредактируйте с безопасными паролями
nano .env
```

### Рекомендуемые настройки для продакшена
```bash
# .env файл для продакшена
DB_PASSWORD=your_very_secure_database_password_here
JWT_SECRET=your_very_secure_jwt_secret_at_least_256_bits_long
FRONTEND_URL=https://your-domain.com
```

## Масштабирование

### Горизонтальное масштабирование
```bash
# Запуск нескольких экземпляров backend
docker-compose up -d --scale backend=3
```

### Настройка ресурсов
```yaml
# В docker-compose.yml добавьте:
services:
  backend:
    deploy:
      resources:
        limits:
          memory: 1G
          cpus: '0.5'
        reservations:
          memory: 512M
          cpus: '0.25'
```

## Резервное копирование

### Автоматическое резервное копирование
```bash
#!/bin/bash
# backup.sh
DATE=$(date +%Y%m%d_%H%M%S)
docker-compose exec -T postgres pg_dump -U construction_user construction_monitoring > "backup_$DATE.sql"
gzip "backup_$DATE.sql"
```

### Настройка cron для автоматических бэкапов
```bash
# Добавьте в crontab
0 2 * * * /path/to/backup.sh
```

## Производительность

### Оптимизация PostgreSQL
```yaml
# В docker-compose.yml добавьте:
services:
  postgres:
    command: >
      postgres
      -c shared_buffers=256MB
      -c effective_cache_size=1GB
      -c maintenance_work_mem=64MB
      -c checkpoint_completion_target=0.9
      -c wal_buffers=16MB
      -c default_statistics_target=100
```

### Оптимизация Java приложения
```yaml
services:
  backend:
    environment:
      - JAVA_OPTS=-Xms512m -Xmx2g -XX:+UseG1GC
```

## Мониторинг

### Health checks
```bash
# Проверка здоровья сервисов
curl http://localhost:8080/actuator/health
curl http://localhost/api/health
```

### Метрики
```bash
# Просмотр метрик
curl http://localhost:8080/actuator/metrics
```


