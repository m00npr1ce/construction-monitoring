# Резервное копирование БД (PostgreSQL)

## Ручной бэкап
```bash
# В корне проекта (compose.dev)
docker exec -t construction-monitoring-postgres-1 \
  pg_dump -U postgres -d construction_monitoring \
  -F c -f /tmp/backup_$(date +%Y%m%d_%H%M).dump

# Скопировать файл
docker cp construction-monitoring-postgres-1:/tmp/backup_20250101_0000.dump ./backups/
```

## Восстановление
```bash
docker exec -i construction-monitoring-postgres-1 \
  pg_restore -U postgres -d construction_monitoring -c < ./backups/backup_20250101_0000.dump
```

## Автоматизация (cron на хосте)
- Создать каталог `backups/` и планировщик (Windows: Task Scheduler; Linux: cron) для ежедневного запуска команды `pg_dump` с датированной меткой.
- Хранить не менее 7 последних копий.

## Замечания
- Доступы берутся из docker‑compose (логин/пароль/БД).
- Для production хранить бэкапы вне контейнера (том/облачное хранилище) и шифровать.
