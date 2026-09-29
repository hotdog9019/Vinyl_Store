# ER Diagram

```text
users
-----
id PK
full_name
email UNIQUE
phone

orders
------
id PK
user_id FK -- users.id
album_title
artist
quantity
total_price
status
created_at
```

Связь:

```text
users 1 ---- orders
```

Один пользователь может иметь много заказов. Каждый заказ обязательно связан с существующим пользователем.
