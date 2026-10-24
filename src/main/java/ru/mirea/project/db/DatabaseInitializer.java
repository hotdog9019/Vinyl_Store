package ru.mirea.project.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {
    private DatabaseInitializer() {
    }

    public static void initialize() throws SQLException {
        try (Connection connection = DatabaseManager.getConnection();
             Statement statement = connection.createStatement()) {
            createAndRepairUsers(statement);
            createAndRepairVinyl(statement);
            createAndRepairOrders(statement);
        }
    }

    private static void createAndRepairVinyl(Statement statement) throws SQLException {
        statement.executeUpdate("""
                create table if not exists vinyl (
                    id bigserial primary key,
                    title varchar(160) not null,
                    artist varchar(120) not null,
                    genre varchar(80),
                    price numeric(10, 2) not null check (price > 0),
                    stock integer not null default 0 check (stock >= 0)
                )
                """);
    }

    private static void createAndRepairUsers(Statement statement) throws SQLException {
        statement.executeUpdate("""
                create table if not exists users (
                    id bigserial primary key
                )
                """);
        statement.executeUpdate("alter table users add column if not exists full_name varchar(120)");
        statement.executeUpdate("alter table users add column if not exists email varchar(120)");
        statement.executeUpdate("alter table users add column if not exists phone varchar(30)");
        statement.executeUpdate("""
                do $$
                begin
                    if exists (
                        select 1
                        from information_schema.columns
                        where table_schema = 'public'
                          and table_name = 'users'
                          and column_name = 'name'
                    ) then
                        update users
                        set full_name = name
                        where full_name is null and name is not null;

                        update users
                        set name = coalesce(nullif(full_name, ''), 'User ' || id)
                        where name is null or trim(name) = '';
                    end if;
                end $$;
                """);
        statement.executeUpdate("update users set full_name = 'User ' || id where full_name is null or trim(full_name) = ''");
        statement.executeUpdate("update users set email = 'user' || id || '@example.com' where email is null or trim(email) = ''");
        statement.executeUpdate("update users set phone = '+7 000 000-00-' || lpad(id::text, 2, '0') where phone is null or trim(phone) = ''");
    }

    private static void createAndRepairOrders(Statement statement) throws SQLException {

        statement.executeUpdate("""
                create table if not exists orders (
                    id bigserial primary key
                )
                """);
        statement.executeUpdate("alter table orders add column if not exists user_id bigint");
        statement.executeUpdate("alter table orders add column if not exists album_title varchar(160)");
        statement.executeUpdate("alter table orders add column if not exists artist varchar(120)");
        statement.executeUpdate("alter table orders add column if not exists quantity integer");
        statement.executeUpdate("alter table orders add column if not exists total_price numeric(10, 2)");
        statement.executeUpdate("alter table orders add column if not exists status varchar(20)");
        statement.executeUpdate("alter table orders add column if not exists created_at timestamp default current_timestamp");
        statement.executeUpdate("""
                do $$
                begin
                    if exists (
                        select 1 from information_schema.columns
                        where table_schema = 'public' and table_name = 'orders' and column_name = 'vinyl_title'
                    ) then
                        update orders set album_title = vinyl_title
                        where (album_title is null or trim(album_title) = '') and vinyl_title is not null;
                    end if;

                    if exists (
                        select 1 from information_schema.columns
                        where table_schema = 'public' and table_name = 'orders' and column_name = 'price'
                    ) then
                        update orders set total_price = price
                        where total_price is null and price is not null;
                    end if;
                end $$;
                """);
        statement.executeUpdate("update orders set album_title = 'Unknown album' where album_title is null or trim(album_title) = ''");
        statement.executeUpdate("update orders set album_title = U&'\\041D\\0435 \\0443\\043A\\0430\\0437\\0430\\043D\\043E' where album_title like U&'\\0420%'");
        statement.executeUpdate("update orders set album_title = 'Abbey Road' where lower(artist) = 'the beatles' and album_title in ('Unknown album', U&'\\041D\\0435 \\0443\\043A\\0430\\0437\\0430\\043D\\043E')");
        statement.executeUpdate("update orders set album_title = 'Thriller' where lower(artist) = 'michael jackson' and album_title in ('Unknown album', U&'\\041D\\0435 \\0443\\043A\\0430\\0437\\0430\\043D\\043E')");
        statement.executeUpdate("update orders set album_title = 'Back to Black' where lower(artist) = 'amy winehouse' and album_title in ('Unknown album', U&'\\041D\\0435 \\0443\\043A\\0430\\0437\\0430\\043D\\043E')");
        statement.executeUpdate("update orders set album_title = 'Rumours' where lower(artist) = 'fleetwood mac' and album_title in ('Unknown album', U&'\\041D\\0435 \\0443\\043A\\0430\\0437\\0430\\043D\\043E')");
        statement.executeUpdate("update orders set album_title = 'Random Access Memories' where lower(artist) = 'daft punk' and album_title in ('Unknown album', U&'\\041D\\0435 \\0443\\043A\\0430\\0437\\0430\\043D\\043E')");
        statement.executeUpdate("update orders set album_title = 'Nevermind' where lower(artist) = 'nirvana' and album_title in ('Unknown album', U&'\\041D\\0435 \\0443\\043A\\0430\\0437\\0430\\043D\\043E')");
        statement.executeUpdate("update orders set album_title = 'The Dark Side of the Moon' where lower(artist) = 'pink floyd' and album_title in ('Unknown album', U&'\\041D\\0435 \\0443\\043A\\0430\\0437\\0430\\043D\\043E')");
        statement.executeUpdate("update orders set artist = 'Unknown artist' where artist is null or trim(artist) = ''");
        statement.executeUpdate("update orders set quantity = 1 where quantity is null or quantity <= 0");
        statement.executeUpdate("update orders set total_price = 1 where total_price is null or total_price <= 0");
        statement.executeUpdate("update orders set created_at = current_timestamp where created_at is null");
        statement.executeUpdate("update orders set status = 'NEW' where status is null or trim(status) = ''");
        statement.executeUpdate("update orders set status = 'NEW' where upper(status) = 'CREATED'");
        statement.executeUpdate("update orders set status = 'CANCELED' where upper(status) = 'CANCELLED'");
    }
}
