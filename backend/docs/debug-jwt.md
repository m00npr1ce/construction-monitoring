Инструкция по отладке JWT аутентификации и создания дефекта

1) Сборка и запуск
   Остановите текущее приложение (если запущено в IntelliJ или терминале).
   В PowerShell из корня репозитория выполните:

   Set-Location 'C:\Users\maksa\IdeaProjects\construction-monitoring'
   .\backend\mvnw.cmd -f backend/pom.xml -DskipTests spring-boot:run

2) Проверка логов старта
   Убедитесь, что Flyway валидировал 4 миграции и приложение запустилось.
   Ищите в логах: "Successfully validated 4 migrations" и "Started BackendApplication".

3) Быстрая проверка токена
   curl.exe -v -H "Authorization: Bearer <TOKEN>" http://localhost:8080/api/test/me
   Ожидается JSON с username и roles.

4) Отладочный POST
   curl.exe -v -X POST "http://localhost:8080/api/defects" `
     -H "Authorization: Bearer <TOKEN>" `
     -H "Content-Type: application/json" `
     -d "{\"title\":\"Leak in ceiling\",\"description\":\"Water leak on 3rd floor\",\"projectId\":1}"

5) Что смотрим в логах
   - В `JwtAuthenticationFilter` появится строка с "JWT validated: authenticated='alice' authorities=[...] path=/api/defects" — это означает, что токен принят и SecurityContext заполнен.
   - Если такого лога нет, скопируйте последние ~50 строк логов и пришлите мне.

6) Если POST вернул 403, но в логах есть запись о валидации JWT и затем SQL/validation exception — пришлите эти логи (40-60 строк вокруг времени запроса).

7) После отладки (по желанию)
   - Можно удалить отладочный лог из `JwtAuthenticationFilter` и закоммитить чистую версию.


Автор: автоматизированный помощник — временная инструкция для локальной отладки.
