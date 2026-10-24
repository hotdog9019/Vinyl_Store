package ru.mirea.project.repository;

import ru.mirea.project.db.DatabaseManager;
import ru.mirea.project.model.Order;
import ru.mirea.project.model.OrderStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrderRepository implements CrudRepository<Order> {
    @Override
    public long create(Order order) throws SQLException {
        try (Connection connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                if (order.getStatus() == OrderStatus.COMPLETED) {
                    decreaseVinylStock(connection, order);
                }

                boolean legacyColumns = hasColumn(connection, "vinyl_title") && hasColumn(connection, "price");
                String sql = legacyColumns
                        ? """
                          insert into orders
                              (user_id, album_title, vinyl_title, artist, quantity, total_price, price, status)
                          values (?, ?, ?, ?, ?, ?, ?, ?)
                          """
                        : """
                          insert into orders (user_id, album_title, artist, quantity, total_price, status)
                          values (?, ?, ?, ?, ?, ?)
                          """;
                try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    if (legacyColumns) {
                        fillLegacyOrderFields(statement, order);
                    } else {
                        fillOrderFields(statement, order);
                    }
                    statement.executeUpdate();
                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (keys.next()) {
                            long id = keys.getLong(1);
                            connection.commit();
                            return id;
                        }
                        throw new SQLException("База данных не вернула id созданного заказа.");
                    }
                }
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    @Override
    public List<Order> findAll() throws SQLException {
        String sql = """
                select id, user_id, album_title, artist, quantity, total_price, status, created_at
                from orders
                order by created_at desc
                """;
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            return mapOrders(resultSet);
        }
    }

    @Override
    public Optional<Order> findById(long id) throws SQLException {
        String sql = """
                select id, user_id, album_title, artist, quantity, total_price, status, created_at
                from orders
                where id = ?
                """;
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapOrder(resultSet));
                }
                return Optional.empty();
            }
        }
    }

    public List<Order> search(String query) throws SQLException {
        return searchByAlbumOrArtist(query);
    }

    public List<Order> searchByAlbum(String query) throws SQLException {
        String sql = """
                select id, user_id, album_title, artist, quantity, total_price, status, created_at
                from orders
                where lower(album_title) like lower(?)
                order by created_at desc
                """;
        return searchByOneParameter(sql, query);
    }

    public List<Order> searchByArtist(String query) throws SQLException {
        String sql = """
                select id, user_id, album_title, artist, quantity, total_price, status, created_at
                from orders
                where lower(artist) like lower(?)
                order by created_at desc
                """;
        return searchByOneParameter(sql, query);
    }

    public List<Order> searchByAlbumOrArtist(String query) throws SQLException {
        String sql = """
                select id, user_id, album_title, artist, quantity, total_price, status, created_at
                from orders
                where lower(album_title) like lower(?) or lower(artist) like lower(?)
                order by created_at desc
                """;
        String pattern = "%" + query + "%";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, pattern);
            statement.setString(2, pattern);
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapOrders(resultSet);
            }
        }
    }

    private List<Order> searchByOneParameter(String sql, String query) throws SQLException {
        String pattern = "%" + query + "%";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, pattern);
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapOrders(resultSet);
            }
        }
    }

    public List<Order> findByStatus(OrderStatus status) throws SQLException {
        String sql = """
                select id, user_id, album_title, artist, quantity, total_price, status, created_at
                from orders
                where status = ?
                order by created_at desc
                """;
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.name());
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapOrders(resultSet);
            }
        }
    }

    public List<Order> findByUserId(long userId) throws SQLException {
        String sql = """
                select id, user_id, album_title, artist, quantity, total_price, status, created_at
                from orders
                where user_id = ?
                order by created_at desc
                """;
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapOrders(resultSet);
            }
        }
    }

    public List<Order> findAllOrderByDateDesc() throws SQLException {
        return findAll();
    }

    @Override
    public boolean update(Order order) throws SQLException {
        try (Connection connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Optional<Order> existing = findByIdForUpdate(connection, order.getId());
                if (existing.isEmpty()) {
                    connection.rollback();
                    return false;
                }

                if (existing.get().getStatus() != OrderStatus.COMPLETED
                        && order.getStatus() == OrderStatus.COMPLETED) {
                    decreaseVinylStock(connection, order);
                }

                boolean legacyColumns = hasColumn(connection, "vinyl_title") && hasColumn(connection, "price");
                String sql = legacyColumns
                        ? """
                          update orders set user_id = ?, album_title = ?, vinyl_title = ?, artist = ?,
                              quantity = ?, total_price = ?, price = ?, status = ? where id = ?
                          """
                        : """
                          update orders set user_id = ?, album_title = ?, artist = ?, quantity = ?,
                              total_price = ?, status = ? where id = ?
                          """;
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    if (legacyColumns) {
                        fillLegacyOrderFields(statement, order);
                        statement.setLong(9, order.getId());
                    } else {
                        fillOrderFields(statement, order);
                        statement.setLong(7, order.getId());
                    }
                    boolean updated = statement.executeUpdate() > 0;
                    connection.commit();
                    return updated;
                }
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    @Override
    public boolean delete(long id) throws SQLException {
        try (Connection connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Optional<Order> existing = findByIdForUpdate(connection, id);
                if (existing.isEmpty()) {
                    connection.rollback();
                    return false;
                }
                if (existing.get().getStatus() == OrderStatus.COMPLETED) {
                    increaseVinylStock(connection, existing.get());
                }
                try (PreparedStatement statement = connection.prepareStatement("delete from orders where id = ?")) {
                    statement.setLong(1, id);
                    boolean deleted = statement.executeUpdate() > 0;
                    connection.commit();
                    return deleted;
                }
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    private void fillOrderFields(PreparedStatement statement, Order order) throws SQLException {
        statement.setLong(1, order.getUserId());
        statement.setString(2, order.getAlbumTitle());
        statement.setString(3, order.getArtist());
        statement.setInt(4, order.getQuantity());
        statement.setBigDecimal(5, order.getTotalPrice());
        statement.setString(6, order.getStatus().name());
    }

    private void fillLegacyOrderFields(PreparedStatement statement, Order order) throws SQLException {
        statement.setLong(1, order.getUserId());
        statement.setString(2, order.getAlbumTitle());
        statement.setString(3, order.getAlbumTitle());
        statement.setString(4, order.getArtist());
        statement.setInt(5, order.getQuantity());
        statement.setBigDecimal(6, order.getTotalPrice());
        statement.setBigDecimal(7, order.getTotalPrice());
        statement.setString(8, order.getStatus().name());
    }

    private boolean hasColumn(Connection connection, String columnName) throws SQLException {
        String sql = """
                select 1 from information_schema.columns
                where table_schema = 'public' and table_name = 'orders' and column_name = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, columnName);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private Optional<Order> findByIdForUpdate(Connection connection, long id) throws SQLException {
        String sql = """
                select id, user_id, album_title, artist, quantity, total_price, status, created_at
                from orders where id = ? for update
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapOrder(resultSet)) : Optional.empty();
            }
        }
    }

    private void decreaseVinylStock(Connection connection, Order order) throws SQLException {
        String sql = """
                update vinyl
                set stock = stock - ?
                where id = (
                    select v.id from vinyl v
                    where lower(v.title) = lower(?) and lower(v.artist) = lower(?)
                    order by v.id limit 1
                ) and stock >= ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, order.getQuantity());
            statement.setString(2, order.getAlbumTitle());
            statement.setString(3, order.getArtist());
            statement.setInt(4, order.getQuantity());
            if (statement.executeUpdate() == 0) {
                throw new SQLException("Пластинка не найдена в каталоге или на складе недостаточно экземпляров.");
            }
        }
    }

    private void increaseVinylStock(Connection connection, Order order) throws SQLException {
        String sql = """
                update vinyl
                set stock = stock + ?
                where id = (
                    select v.id from vinyl v
                    where lower(v.title) = lower(?) and lower(v.artist) = lower(?)
                    order by v.id limit 1
                )
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, order.getQuantity());
            statement.setString(2, order.getAlbumTitle());
            statement.setString(3, order.getArtist());
            statement.executeUpdate();
        }
    }

    private List<Order> mapOrders(ResultSet resultSet) throws SQLException {
        List<Order> orders = new ArrayList<>();
        while (resultSet.next()) {
            orders.add(mapOrder(resultSet));
        }
        return orders;
    }

    private Order mapOrder(ResultSet resultSet) throws SQLException {
        Timestamp createdAt = resultSet.getTimestamp("created_at");
        return new Order(
                resultSet.getLong("id"),
                resultSet.getLong("user_id"),
                resultSet.getString("album_title"),
                resultSet.getString("artist"),
                resultSet.getInt("quantity"),
                resultSet.getBigDecimal("total_price"),
                OrderStatus.fromDatabaseValue(resultSet.getString("status")),
                createdAt.toLocalDateTime()
        );
    }
}
