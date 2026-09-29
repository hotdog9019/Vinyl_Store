delete from users
where lower(email) in (
    'ivan@example.com',
    'maria@example.com',
    'artem@example.com'
);
