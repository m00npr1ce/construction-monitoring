# Construction Monitoring System

Система централизованного управления дефектами на строительных объектах для ООО «СистемаКонтроля».

## Описание

Монолитное веб-приложение для полного цикла работы с дефектами: от регистрации и назначения исполнителя до контроля статусов и формирования отчётности для руководства.

## Функциональность

### Основные возможности
- ✅ Регистрация пользователей и аутентификация
- ✅ Разграничение прав доступа (менеджер, инженер, наблюдатель)
- ✅ Управление проектами/объектами и их этапами
- ✅ Создание и редактирование дефектов
- ✅ Управление статусами дефектов: Новая → В работе → На проверке → Закрыта/Отменена
- ✅ Ведение комментариев и истории изменений
- ✅ Поиск, сортировка и фильтрация дефектов
- ✅ Экспорт отчётности в Excel
- ✅ Просмотр аналитических отчётов

### Роли пользователей
- **Инженеры**: регистрация дефектов, обновление информации
- **Менеджеры**: назначение задач, контроль сроков, формирование отчётов
- **Руководители**: просмотр прогресса и отчётности

## Технологический стек

### Backend
- Java 21
- Spring Boot 3.5.6
- Spring Security
- Spring Data JPA
- PostgreSQL
- Flyway (миграции БД)
- JWT для аутентификации
- Apache POI для экспорта Excel
- Maven

### Frontend
- Vue 3
- TypeScript
- Vite
- Vue Router
- Pinia
- Tailwind CSS
- Axios

## Установка и запуск

### Предварительные требования
- Java 21+
- Node.js 20+
- PostgreSQL 12+
- Maven 3.6+

### Backend

1. Клонируйте репозиторий:
```bash
git clone <repository-url>
cd construction-monitoring/backend
```

2. Настройте базу данных PostgreSQL:
```sql
CREATE DATABASE construction_monitoring;
CREATE USER construction_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE construction_monitoring TO construction_user;
```

3. Настройте переменные окружения:
```bash
export JWT_SECRET=your-very-strong-secret-key
export FRONTEND_URL=http://localhost:5173
```

4. Запустите приложение:
```bash
mvn spring-boot:run
```

Backend будет доступен по адресу: http://localhost:8080

### Frontend

1. Перейдите в папку frontend:
```bash
cd ../frontend
```

2. Установите зависимости:
```bash
npm install
```

3. Запустите в режиме разработки:
```bash
npm run dev
```

Frontend будет доступен по адресу: http://localhost:5173

## API Документация

### Аутентификация
- `POST /api/auth/register` - Регистрация пользователя
- `POST /api/auth/login` - Вход в систему
- `GET /api/test/me` - Получение информации о текущем пользователе

### Проекты
- `GET /api/projects` - Список проектов
- `POST /api/projects` - Создание проекта
- `GET /api/projects/{id}` - Получение проекта
- `PUT /api/projects/{id}` - Обновление проекта
- `DELETE /api/projects/{id}` - Удаление проекта

### Дефекты
- `GET /api/defects` - Список дефектов
- `POST /api/defects` - Создание дефекта
- `GET /api/defects/{id}` - Получение дефекта
- `PUT /api/defects/{id}` - Обновление дефекта
- `DELETE /api/defects/{id}` - Удаление дефекта

### Отчёты
- `GET /api/reports/defects/export` - Экспорт дефектов в Excel
- `GET /api/reports/analytics` - Аналитические данные

### Файлы
- `POST /api/attachments/upload/{defectId}` - Загрузка файла
- `GET /api/attachments/defect/{defectId}` - Список файлов дефекта
- `GET /api/attachments/{id}/download` - Скачивание файла
- `DELETE /api/attachments/{id}` - Удаление файла

### Комментарии
- `POST /api/comments` - Создание комментария
- `GET /api/comments/defect/{defectId}` - Список комментариев дефекта
- `DELETE /api/comments/{id}` - Удаление комментария

## Тестирование

### Запуск тестов
```bash
# Backend тесты
cd backend
mvn test

# Frontend тесты
cd frontend
npm run test
```

### Покрытие тестами
- Unit тесты: 5+ тестов для основных сервисов
- Интеграционные тесты: 2+ сценария полного workflow
- Тестирование безопасности: валидация JWT, проверка прав доступа

## Безопасность

### Реализованные меры
- ✅ Хеширование паролей с помощью BCrypt
- ✅ JWT токены для аутентификации
- ✅ Валидация входных данных
- ✅ Защита от SQL-инъекций (JPA)
- ✅ CORS настройки
- ✅ Глобальная обработка исключений

### Рекомендации для продакшена
- Используйте HTTPS
- Настройте сильный JWT secret
- Регулярно обновляйте зависимости
- Настройте мониторинг и логирование
- Используйте reverse proxy (nginx)

## Развертывание

### Docker (рекомендуется)
```bash
# Создайте docker-compose.yml
version: '3.8'
services:
  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: construction_monitoring
      POSTGRES_USER: construction_user
      POSTGRES_PASSWORD: your_password
    ports:
      - "5432:5432"
  
  backend:
    build: ./backend
    ports:
      - "8080:8080"
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/construction_monitoring
      - JWT_SECRET=your-very-strong-secret-key
    depends_on:
      - postgres
  
  frontend:
    build: ./frontend
    ports:
      - "5173:5173"
    depends_on:
      - backend
```

### Ручное развертывание
1. Соберите backend: `mvn clean package`
2. Соберите frontend: `npm run build`
3. Настройте веб-сервер (nginx/Apache)
4. Настройте SSL сертификаты
5. Настройте резервное копирование БД

## Мониторинг и логирование

### Логирование
- Spring Boot Actuator для мониторинга
- Структурированные логи в JSON формате
- Логирование всех операций с дефектами

### Метрики
- Время отклика API
- Количество активных пользователей
- Статистика по дефектам
- Использование ресурсов

## Поддержка и сопровождение

### Резервное копирование
- Автоматическое резервное копирование БД (настраивается в cron)
- Хранение файлов в отдельной директории
- Версионирование миграций БД

### Обновления
- Миграции БД через Flyway
- Версионирование API
- Обратная совместимость

## Лицензия

Проприетарное программное обеспечение ООО «СистемаКонтроля».

## Контакты

Для вопросов по системе обращайтесь к команде разработки.