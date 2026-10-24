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
- JavaFX-клиент для КР №2 с прямым JDBC-подключением к PostgreSQL.

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

## Запуск JavaFX-клиента

Перед запуском поднимите PostgreSQL:

```bash
docker compose up -d
```

Затем запустите настольное приложение:

```bash
mvn javafx:run
```

Главный класс JavaFX-приложения:

```text
ru.mirea.project.fx.VinylStoreFxApplication
```

В интерфейсе доступны обзор статистики, таблицы пользователей, заказов и каталога пластинок, CRUD-операции, поиск заказов, фильтры, сортировка и экспорт данных в Excel. При создании заказа пластинка выбирается из каталога, а сумма рассчитывается по цене и количеству. Телефоны пользователей проверяются и сохраняются в формате `+7XXXXXXXXXX`.
Ключевой интерфейс вынесен в FXML: `src/main/resources/ru/mirea/project/fx/main-view.fxml`.
Обработчики JavaFX находятся в `src/main/java/ru/mirea/project/fx/MainController.java`.

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

Также параметры лежат в `src/main/resources/database.properties`:

```text
db.url=jdbc:postgresql://localhost:5432/vinyl_store
db.user=postgres
db.password=1234
```

Переменные окружения имеют приоритет над файлом настроек.

ER-описание находится в `docs/er-diagram.md`.
