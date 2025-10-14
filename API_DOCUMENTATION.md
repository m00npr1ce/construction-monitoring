# API Документация

## Базовый URL
```
http://localhost:8080/api
```

## Аутентификация

Все запросы к защищенным эндпоинтам должны содержать JWT токен в заголовке:
```
Authorization: Bearer <token>
```

## Эндпоинты

### Аутентификация

#### POST /auth/register
Регистрация нового пользователя.

**Запрос:**
```json
{
  "username": "string",
  "password": "string"
}
```

**Ответ:**
```json
"registered"
```

**Коды ответа:**
- `200` - Успешная регистрация
- `400` - Неверные данные или пользователь уже существует

#### POST /auth/login
Вход в систему.

**Запрос:**
```json
{
  "username": "string",
  "password": "string"
}
```

**Ответ:**
```json
{
  "token": "string"
}
```

**Коды ответа:**
- `200` - Успешный вход
- `401` - Неверные учетные данные

#### GET /test/me
Получение информации о текущем пользователе.

**Заголовки:**
```
Authorization: Bearer <token>
```

**Ответ:**
```json
{
  "username": "string",
  "roles": ["string"]
}
```

### Проекты

#### GET /projects
Получение списка всех проектов.

**Заголовки:**
```
Authorization: Bearer <token>
```

**Ответ:**
```json
[
  {
    "id": 1,
    "name": "string",
    "description": "string",
    "startDate": "2024-01-01",
    "endDate": "2024-12-31"
  }
]
```

#### POST /projects
Создание нового проекта.

**Заголовки:**
```
Authorization: Bearer <token>
Content-Type: application/json
```

**Запрос:**
```json
{
  "name": "string",
  "description": "string",
  "startDate": "2024-01-01",
  "endDate": "2024-12-31"
}
```

**Ответ:**
```json
{
  "id": 1,
  "name": "string",
  "description": "string",
  "startDate": "2024-01-01",
  "endDate": "2024-12-31"
}
```

**Коды ответа:**
- `201` - Проект создан
- `400` - Неверные данные

#### GET /projects/{id}
Получение проекта по ID.

**Заголовки:**
```
Authorization: Bearer <token>
```

**Ответ:**
```json
{
  "id": 1,
  "name": "string",
  "description": "string",
  "startDate": "2024-01-01",
  "endDate": "2024-12-31"
}
```

#### PUT /projects/{id}
Обновление проекта.

**Заголовки:**
```
Authorization: Bearer <token>
Content-Type: application/json
```

**Запрос:**
```json
{
  "name": "string",
  "description": "string",
  "startDate": "2024-01-01",
  "endDate": "2024-12-31"
}
```

#### DELETE /projects/{id}
Удаление проекта.

**Заголовки:**
```
Authorization: Bearer <token>
```

**Коды ответа:**
- `204` - Проект удален
- `404` - Проект не найден

### Дефекты

#### GET /defects
Получение списка дефектов.

**Параметры запроса:**
- `projectId` (optional) - Фильтр по проекту

**Заголовки:**
```
Authorization: Bearer <token>
```

**Ответ:**
```json
[
  {
    "id": 1,
    "title": "string",
    "description": "string",
    "priority": "LOW|MEDIUM|HIGH|CRITICAL",
    "status": "NEW|IN_PROGRESS|IN_REVIEW|CLOSED|CANCELLED",
    "assigneeId": 1,
    "projectId": 1,
    "dueDate": "2024-01-01T00:00:00Z",
    "createdAt": "2024-01-01T00:00:00Z",
    "updatedAt": "2024-01-01T00:00:00Z"
  }
]
```

#### POST /defects
Создание нового дефекта.

**Заголовки:**
```
Authorization: Bearer <token>
Content-Type: application/json
```

**Запрос:**
```json
{
  "title": "string",
  "description": "string",
  "priority": "LOW|MEDIUM|HIGH|CRITICAL",
  "status": "NEW|IN_PROGRESS|IN_REVIEW|CLOSED|CANCELLED",
  "assigneeId": 1,
  "projectId": 1,
  "dueDate": "2024-01-01T00:00:00Z"
}
```

**Ответ:**
```json
{
  "id": 1,
  "title": "string",
  "description": "string",
  "priority": "MEDIUM",
  "status": "NEW",
  "assigneeId": 1,
  "projectId": 1,
  "dueDate": "2024-01-01T00:00:00Z",
  "createdAt": "2024-01-01T00:00:00Z",
  "updatedAt": "2024-01-01T00:00:00Z"
}
```

**Коды ответа:**
- `201` - Дефект создан
- `400` - Неверные данные

#### GET /defects/{id}
Получение дефекта по ID.

**Заголовки:**
```
Authorization: Bearer <token>
```

**Ответ:**
```json
{
  "id": 1,
  "title": "string",
  "description": "string",
  "priority": "HIGH",
  "status": "IN_PROGRESS",
  "assigneeId": 1,
  "projectId": 1,
  "dueDate": "2024-01-01T00:00:00Z",
  "createdAt": "2024-01-01T00:00:00Z",
  "updatedAt": "2024-01-01T00:00:00Z"
}
```

#### PUT /defects/{id}
Обновление дефекта.

**Заголовки:**
```
Authorization: Bearer <token>
Content-Type: application/json
```

**Запрос:**
```json
{
  "title": "string",
  "description": "string",
  "priority": "HIGH",
  "status": "IN_PROGRESS",
  "assigneeId": 1,
  "projectId": 1,
  "dueDate": "2024-01-01T00:00:00Z"
}
```

#### DELETE /defects/{id}
Удаление дефекта.

**Заголовки:**
```
Authorization: Bearer <token>
```

**Коды ответа:**
- `204` - Дефект удален
- `404` - Дефект не найден

### Пользователи

#### GET /users
Получение списка пользователей.

**Заголовки:**
```
Authorization: Bearer <token>
```

**Ответ:**
```json
[
  {
    "id": 1,
    "username": "string",
    "role": "ROLE_ENGINEER|ROLE_MANAGER|ROLE_ADMIN"
  }
]
```

### Файлы

#### POST /attachments/upload/{defectId}
Загрузка файла к дефекту.

**Заголовки:**
```
Authorization: Bearer <token>
Content-Type: multipart/form-data
```

**Параметры:**
- `file` - Файл для загрузки

**Ответ:**
```json
{
  "id": 1,
  "fileName": "string",
  "originalFileName": "string",
  "contentType": "string",
  "fileSize": 1024,
  "filePath": "string",
  "defectId": 1,
  "uploadedAt": "2024-01-01T00:00:00Z"
}
```

#### GET /attachments/defect/{defectId}
Получение списка файлов дефекта.

**Заголовки:**
```
Authorization: Bearer <token>
```

**Ответ:**
```json
[
  {
    "id": 1,
    "fileName": "string",
    "originalFileName": "string",
    "contentType": "string",
    "fileSize": 1024,
    "defectId": 1,
    "uploadedAt": "2024-01-01T00:00:00Z"
  }
]
```

#### GET /attachments/{id}/download
Скачивание файла.

**Заголовки:**
```
Authorization: Bearer <token>
```

**Ответ:**
Файл в бинарном формате с соответствующими заголовками Content-Type и Content-Disposition.

#### DELETE /attachments/{id}
Удаление файла.

**Заголовки:**
```
Authorization: Bearer <token>
```

**Коды ответа:**
- `200` - Файл удален
- `404` - Файл не найден

### Комментарии

#### POST /comments
Создание комментария к дефекту.

**Заголовки:**
```
Authorization: Bearer <token>
Content-Type: application/json
```

**Запрос:**
```json
{
  "content": "string",
  "defectId": 1
}
```

**Ответ:**
```json
{
  "id": 1,
  "content": "string",
  "defectId": 1,
  "authorId": 1,
  "createdAt": "2024-01-01T00:00:00Z"
}
```

#### GET /comments/defect/{defectId}
Получение комментариев дефекта.

**Заголовки:**
```
Authorization: Bearer <token>
```

**Ответ:**
```json
[
  {
    "id": 1,
    "content": "string",
    "defectId": 1,
    "authorId": 1,
    "createdAt": "2024-01-01T00:00:00Z"
  }
]
```

#### DELETE /comments/{id}
Удаление комментария.

**Заголовки:**
```
Authorization: Bearer <token>
```

**Коды ответа:**
- `200` - Комментарий удален
- `404` - Комментарий не найден

### Отчёты

#### GET /reports/defects/export
Экспорт дефектов в Excel.

**Параметры запроса:**
- `projectId` (optional) - Фильтр по проекту

**Заголовки:**
```
Authorization: Bearer <token>
```

**Ответ:**
Excel файл (.xlsx) с данными о дефектах.

#### GET /reports/analytics
Получение аналитических данных.

**Параметры запроса:**
- `projectId` (optional) - Фильтр по проекту

**Заголовки:**
```
Authorization: Bearer <token>
```

**Ответ:**
```json
{
  "totalDefects": 100,
  "statusDistribution": {
    "NEW": 20,
    "IN_PROGRESS": 30,
    "IN_REVIEW": 10,
    "CLOSED": 35,
    "CANCELLED": 5
  },
  "priorityDistribution": {
    "LOW": 10,
    "MEDIUM": 50,
    "HIGH": 30,
    "CRITICAL": 10
  },
  "newDefects": 20,
  "inProgressDefects": 30,
  "closedDefects": 35
}
```

## Коды ошибок

### HTTP статус коды
- `200` - OK
- `201` - Created
- `204` - No Content
- `400` - Bad Request
- `401` - Unauthorized
- `403` - Forbidden
- `404` - Not Found
- `500` - Internal Server Error

### Формат ошибок
```json
{
  "error": "string",
  "message": "string",
  "timestamp": "2024-01-01T00:00:00Z"
}
```

## Ограничения

### Размеры файлов
- Максимальный размер файла: 50 MB
- Поддерживаемые форматы: jpg, jpeg, png, gif, pdf, doc, docx, xls, xlsx

### Лимиты API
- Максимальная длина заголовка дефекта: 500 символов
- Максимальная длина описания: 2000 символов
- Максимальная длина комментария: 1000 символов

### Валидация
- Username: 3-50 символов, только буквы, цифры и подчеркивания
- Password: минимум 6 символов
- Title: обязательное поле, максимум 500 символов
- Project ID: обязательное поле для создания дефекта

## Примеры использования

### Создание дефекта с файлом
```bash
# 1. Создание дефекта
curl -X POST http://localhost:8080/api/defects \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Трещина в стене",
    "description": "Обнаружена трещина в несущей стене",
    "priority": "HIGH",
    "projectId": 1
  }'

# 2. Загрузка файла
curl -X POST http://localhost:8080/api/attachments/upload/1 \
  -H "Authorization: Bearer <token>" \
  -F "file=@photo.jpg"
```

### Получение аналитики
```bash
curl -X GET http://localhost:8080/api/reports/analytics \
  -H "Authorization: Bearer <token>"
```

### Экспорт отчёта
```bash
curl -X GET http://localhost:8080/api/reports/defects/export \
  -H "Authorization: Bearer <token>" \
  -o defects_report.xlsx
```




