drop table if exists orders;
drop table if exists vinyl;
drop table if exists users;

create table users (
    id bigserial primary key,
    full_name varchar(50) not null,
    email varchar(120) not null unique,
    phone varchar(30) not null unique
);

create table vinyl (
    id bigserial primary key,
    title varchar(50) not null,
    artist varchar(50) not null,
    genre varchar(30),
    price numeric(10, 2) not null check (price > 0),
    stock integer not null default 0 check (stock >= 0)
);

create table orders (
    id bigserial primary key,
    user_id bigint not null references users(id),
    album_title varchar(160) not null,
    artist varchar(120) not null,
    quantity integer not null check (quantity > 0),
    total_price numeric(10, 2) not null check (total_price > 0),
    status varchar(20) not null check (status in ('NEW', 'PAID', 'SHIPPED', 'COMPLETED', 'CANCELED')),
    created_at timestamp not null default current_timestamp
);

insert into users (full_name, email, phone) values
('Артём Элизбарян', 'artem.eliz@mail.ru', '+79999999999'),
('Анна Климаш', 'anna.klima@mail.ru', '+79999999998'),
('Саша Чобан', 'sacha.choba@mail.ru', '+79999999997'),
('Глеб Логвинов', 'gleb.logvino@mail.ru', '+79999999996'),
('Мария Соколова', 'maria@yandex.ru', '+79002002020');

insert into vinyl (title, artist, genre, price, stock) values
('The Dark Side of the Moon', 'Pink Floyd', 'Rock', 3490.00, 10),
('Kind of Blue', 'Miles Davis', 'Jazz', 2600.00, 8),
('Abbey Road', 'The Beatles', 'Rock', 4100.00, 7),
('Random Access Memories', 'Daft Punk', 'Electronic', 4600.00, 5),
('Nevermind', 'Nirvana', 'Grunge', 3200.00, 9);

insert into orders (user_id, album_title, artist, quantity, total_price, status) values
(1, 'The Dark Side of the Moon', 'Pink Floyd', 1, 3490.00, 'PAID'),
(2, 'Kind of Blue', 'Miles Davis', 2, 5200.00, 'NEW'),
(3, 'Abbey Road', 'The Beatles', 1, 4100.00, 'COMPLETED'),
(4, 'Random Access Memories', 'Daft Punk', 1, 4600.00, 'SHIPPED'),
(5, 'Nevermind', 'Nirvana', 1, 3200.00, 'CANCELED'),
(1, 'Thriller', 'Michael Jackson', 1, 3800.00, 'NEW'),
(2, 'Back in Black', 'AC/DC', 1, 3600.00, 'PAID'),
(3, 'Rumours', 'Fleetwood Mac', 2, 7000.00, 'COMPLETED'),
(4, 'Blue Train', 'John Coltrane', 1, 4200.00, 'SHIPPED'),
(5, 'The Wall', 'Pink Floyd', 1, 5100.00, 'NEW');
