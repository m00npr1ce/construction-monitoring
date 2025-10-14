# Руководство по развертыванию

## Системные требования

### Минимальные требования
- CPU: 2 ядра
- RAM: 4 GB
- Диск: 20 GB свободного места
- ОС: Linux (Ubuntu 20.04+), Windows Server 2019+, macOS 12+

### Рекомендуемые требования
- CPU: 4 ядра
- RAM: 8 GB
- Диск: 50 GB SSD
- ОС: Ubuntu 22.04 LTS

## Подготовка окружения

### 1. Установка Java 21
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-21-jdk

# Проверка
java -version
```

### 2. Установка Node.js 20+
```bash
# Используя NodeSource
curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
sudo apt-get install -y nodejs

# Проверка
node --version
npm --version
```

### 3. Установка PostgreSQL 15
```bash
# Ubuntu/Debian
sudo apt install postgresql postgresql-contrib

# Запуск и включение автозапуска
sudo systemctl start postgresql
sudo systemctl enable postgresql

# Настройка пользователя
sudo -u postgres createuser --interactive
sudo -u postgres createdb construction_monitoring
```

### 4. Установка Maven
```bash
sudo apt install maven
mvn --version
```

## Развертывание Backend

### 1. Сборка приложения
```bash
cd backend
mvn clean package -DskipTests
```

### 2. Настройка базы данных
```sql
-- Подключение к PostgreSQL
sudo -u postgres psql

-- Создание базы данных и пользователя
CREATE DATABASE construction_monitoring;
CREATE USER construction_user WITH PASSWORD 'secure_password_here';
GRANT ALL PRIVILEGES ON DATABASE construction_monitoring TO construction_user;
\q
```

### 3. Настройка переменных окружения
```bash
# Создайте файл .env
cat > .env << EOF
JWT_SECRET=your-very-strong-secret-key-at-least-256-bits
FRONTEND_URL=https://your-domain.com
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/construction_monitoring
SPRING_DATASOURCE_USERNAME=construction_user
SPRING_DATASOURCE_PASSWORD=secure_password_here
EOF
```

### 4. Запуск приложения
```bash
# Загрузка переменных окружения
source .env

# Запуск
java -jar target/backend-0.0.1-SNAPSHOT.jar
```

## Развертывание Frontend

### 1. Сборка приложения
```bash
cd frontend
npm install
npm run build
```

### 2. Настройка Nginx
```nginx
server {
    listen 80;
    server_name your-domain.com;
    
    # Frontend
    location / {
        root /path/to/frontend/dist;
        try_files $uri $uri/ /index.html;
    }
    
    # Backend API
    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
    
    # Файлы
    location /uploads {
        alias /path/to/uploads;
        expires 1y;
        add_header Cache-Control "public, immutable";
    }
}
```

## Docker развертывание

### 1. Dockerfile для Backend
```dockerfile
FROM openjdk:21-jdk-slim

WORKDIR /app
COPY target/backend-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 2. Dockerfile для Frontend
```dockerfile
FROM node:20-alpine as build

WORKDIR /app
COPY package*.json ./
RUN npm ci --only=production

COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf
```

### 3. docker-compose.yml
```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: construction_monitoring
      POSTGRES_USER: construction_user
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./init.sql:/docker-entrypoint-initdb.d/init.sql
    ports:
      - "5432:5432"
    restart: unless-stopped

  backend:
    build: ./backend
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/construction_monitoring
      - SPRING_DATASOURCE_USERNAME=construction_user
      - SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}
      - JWT_SECRET=${JWT_SECRET}
      - FRONTEND_URL=${FRONTEND_URL}
    ports:
      - "8080:8080"
    depends_on:
      - postgres
    restart: unless-stopped
    volumes:
      - uploads:/app/uploads

  frontend:
    build: ./frontend
    ports:
      - "80:80"
    depends_on:
      - backend
    restart: unless-stopped

volumes:
  postgres_data:
  uploads:
```

### 4. Запуск с Docker
```bash
# Создайте .env файл
echo "DB_PASSWORD=secure_password_here" > .env
echo "JWT_SECRET=your-very-strong-secret-key" >> .env
echo "FRONTEND_URL=http://localhost" >> .env

# Запуск
docker-compose up -d
```

## Настройка SSL

### 1. Получение сертификата (Let's Encrypt)
```bash
# Установка Certbot
sudo apt install certbot python3-certbot-nginx

# Получение сертификата
sudo certbot --nginx -d your-domain.com
```

### 2. Настройка Nginx с SSL
```nginx
server {
    listen 443 ssl http2;
    server_name your-domain.com;
    
    ssl_certificate /etc/letsencrypt/live/your-domain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/your-domain.com/privkey.pem;
    
    # Остальная конфигурация...
}

server {
    listen 80;
    server_name your-domain.com;
    return 301 https://$server_name$request_uri;
}
```

## Мониторинг и логирование

### 1. Настройка логирования
```yaml
# application-prod.yml
logging:
  level:
    com.systemcontrol.backend: INFO
    org.springframework.security: WARN
  pattern:
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: /var/log/construction-monitoring/app.log
```

### 2. Настройка мониторинга
```bash
# Установка Prometheus
wget https://github.com/prometheus/prometheus/releases/download/v2.45.0/prometheus-2.45.0.linux-amd64.tar.gz
tar xzf prometheus-2.45.0.linux-amd64.tar.gz
sudo mv prometheus-2.45.0.linux-amd64 /opt/prometheus

# Конфигурация
sudo nano /opt/prometheus/prometheus.yml
```

## Резервное копирование

### 1. Скрипт резервного копирования БД
```bash
#!/bin/bash
# backup-db.sh

DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/backups"
DB_NAME="construction_monitoring"

# Создание резервной копии
pg_dump -h localhost -U construction_user $DB_NAME > $BACKUP_DIR/backup_$DATE.sql

# Сжатие
gzip $BACKUP_DIR/backup_$DATE.sql

# Удаление старых копий (старше 30 дней)
find $BACKUP_DIR -name "backup_*.sql.gz" -mtime +30 -delete

echo "Backup completed: backup_$DATE.sql.gz"
```

### 2. Настройка cron
```bash
# Добавьте в crontab
0 2 * * * /path/to/backup-db.sh
```

## Обновление системы

### 1. Обновление Backend
```bash
# Остановка приложения
sudo systemctl stop construction-monitoring

# Создание резервной копии
./backup-db.sh

# Обновление кода
git pull origin main

# Сборка
mvn clean package -DskipTests

# Запуск
sudo systemctl start construction-monitoring
```

### 2. Обновление Frontend
```bash
# Обновление кода
git pull origin main

# Установка зависимостей
npm install

# Сборка
npm run build

# Перезапуск Nginx
sudo systemctl reload nginx
```

## Устранение неполадок

### 1. Проверка логов
```bash
# Логи приложения
tail -f /var/log/construction-monitoring/app.log

# Логи Nginx
tail -f /var/log/nginx/error.log

# Логи PostgreSQL
tail -f /var/log/postgresql/postgresql-15-main.log
```

### 2. Проверка статуса сервисов
```bash
# Статус приложения
sudo systemctl status construction-monitoring

# Статус PostgreSQL
sudo systemctl status postgresql

# Статус Nginx
sudo systemctl status nginx
```

### 3. Проверка портов
```bash
# Проверка открытых портов
netstat -tlnp | grep :8080
netstat -tlnp | grep :80
netstat -tlnp | grep :5432
```

## Безопасность

### 1. Настройка файрвола
```bash
# UFW
sudo ufw enable
sudo ufw allow 22/tcp
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw deny 8080/tcp
```

### 2. Настройка fail2ban
```bash
sudo apt install fail2ban
sudo systemctl enable fail2ban
sudo systemctl start fail2ban
```

### 3. Регулярные обновления
```bash
# Автоматические обновления безопасности
sudo apt install unattended-upgrades
sudo dpkg-reconfigure -plow unattended-upgrades
```

## Производительность

### 1. Настройка PostgreSQL
```sql
-- postgresql.conf
shared_buffers = 256MB
effective_cache_size = 1GB
maintenance_work_mem = 64MB
checkpoint_completion_target = 0.9
wal_buffers = 16MB
default_statistics_target = 100
```

### 2. Настройка JVM
```bash
# В переменных окружения
export JAVA_OPTS="-Xms512m -Xmx2g -XX:+UseG1GC"
```

### 3. Настройка Nginx
```nginx
# nginx.conf
worker_processes auto;
worker_connections 1024;

http {
    gzip on;
    gzip_types text/plain text/css application/json application/javascript;
    
    client_max_body_size 50M;
}
```



