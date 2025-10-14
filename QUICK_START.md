# Быстрый запуск Construction Monitoring System

## 🚀 Запуск через Docker (рекомендуется)

### Вариант 1: Полный Docker (если есть интернет)
```bash
# Запуск всех сервисов в Docker
docker-compose up -d

# Просмотр логов
docker-compose logs -f

# Остановка
docker-compose down
```

### Вариант 2: Гибридный (PostgreSQL в Docker, приложения локально)
```bash
# 1. Запуск PostgreSQL в Docker
docker-compose -f docker-compose.dev.yml up -d

# 2. Запуск Backend (в отдельном терминале)
cd backend
mvn spring-boot:run

# 3. Запуск Frontend (в отдельном терминале)
cd frontend
npm run dev
```

### Вариант 3: Автоматический запуск (Windows)
```bash
# Запуск скрипта
start-dev.bat
```

### Вариант 4: Автоматический запуск (Linux/Mac)
```bash
# Запуск скрипта
./start-dev.sh
```

## 📍 Доступ к приложению

После запуска система будет доступна по адресам:
- **Frontend**: http://localhost:5173 (или http://localhost для Docker)
- **Backend API**: http://localhost:8080
- **PostgreSQL**: localhost:5432

## 🔧 Управление сервисами

### Проверка статуса
```bash
# Docker контейнеры
docker ps

# Локальные процессы
# Windows
tasklist | findstr "java\|node"

# Linux/Mac
ps aux | grep -E "java|node"
```

### Остановка сервисов
```bash
# Остановка Docker
docker-compose down

# Остановка локальных процессов
# Windows
taskkill /f /im java.exe
taskkill /f /im node.exe

# Linux/Mac
pkill -f "spring-boot"
pkill -f "npm run dev"
```

## 🐛 Устранение неполадок

### Проблемы с портами
```bash
# Проверка занятых портов
# Windows
netstat -an | findstr ":8080\|:5173\|:5432"

# Linux/Mac
netstat -tlnp | grep -E ":8080|:5173|:5432"
```

### Проблемы с Docker
```bash
# Очистка Docker
docker system prune -a

# Перезапуск Docker
docker-compose down
docker-compose up -d
```

### Проблемы с базой данных
```bash
# Сброс базы данных
docker-compose -f docker-compose.dev.yml down
docker volume rm construction-monitoring_postgres_data
docker-compose -f docker-compose.dev.yml up -d
```

## 📊 Мониторинг

### Логи приложений
```bash
# Backend логи
tail -f backend/logs/application.log

# Frontend логи
# Отображаются в консоли при запуске npm run dev

# Docker логи
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f postgres
```

### Проверка здоровья
```bash
# Backend health check
curl http://localhost:8080/actuator/health

# Frontend
curl http://localhost:5173
```

## 🔐 Первый запуск

1. **Регистрация пользователя**:
   - Откройте http://localhost:5173
   - Нажмите "Sign up"
   - Создайте учетную запись

2. **Создание проекта**:
   - Перейдите в раздел "Projects"
   - Нажмите "Create Project"
   - Заполните данные проекта

3. **Создание дефекта**:
   - Перейдите в раздел "Defects"
   - Нажмите "Create Defect"
   - Заполните данные дефекта

## 📝 Настройка для продакшена

Для продакшена используйте:
```bash
# Создайте .env файл с настройками
cp env.example .env

# Отредактируйте настройки
nano .env

# Запуск продакшен версии
docker-compose -f docker-compose.prod.yml up -d
```

## 🆘 Поддержка

При возникновении проблем:
1. Проверьте логи: `docker-compose logs -f`
2. Убедитесь, что все порты свободны
3. Перезапустите сервисы: `docker-compose restart`
4. Обратитесь к документации в `README.md` и `DEPLOYMENT.md`


