# Vinyl Store Console

Консольная информационная система для учета заказов магазина виниловых пластинок.


- Maven-проект на Java 21.
- Подключение к PostgreSQL через JDBC.
- Слои `model`, `repository`, `service`, `db`.
- Интерфейс `CrudRepository`.
- CRUD для пользователей.
- CRUD для заказов.
- Поиск заказов по пластинке, исполнителю, общему тексту и ID.
- Фильтрация заказов по статусу и пользователю.
- Сортировка заказов по сумме и дате.
- Статистика больше чем по пяти показателям.
- Свои исключения для бизнес-ошибок и ненайденных сущностей.
- Экспорт заказов, пластинок и пользователей в `exports/orders.xlsx`.
- Docker Compose для запуска PostgreSQL.

```bash
docker compose up -d
```

В контейнере автоматически создается база `vinyl_store`, пользователь `postgres`, пароль `1234`, а SQL-скрипт `src/main/resources/database.sql` применяется при первом запуске контейнера.

## Запуск базы через Docker

```bash
docker compose up -d
```

Остановить базу:

```bash
docker compose down
```

Полностью удалить данные и создать базу заново:

```bash
docker compose down -v
docker compose up -d
```

## Запуск программы

```bash
mvn compile exec:java
```

Или запустите класс:

```text
ru.mirea.project.Main
```

## Подключение

По умолчанию приложение подключается к:

```text
jdbc:postgresql://localhost:5432/vinyl_store
user: postgres
password: 1234
```

Эти параметры можно заменить переменными окружения:

```text
VINYL_DB_URL
VINYL_DB_USER
VINYL_DB_PASSWORD
```

ER-описание находится в `docs/er-diagram.md`.
