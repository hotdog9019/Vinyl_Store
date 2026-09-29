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
        String sql = """
                insert into orders (user_id, album_title, artist, quantity, total_price, status)
                values (?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillOrderFields(statement, order);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
                throw new SQLException("База данных не вернула id созданного заказа.");
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
        String sql = """
                update orders
                set user_id = ?, album_title = ?, artist = ?, quantity = ?, total_price = ?, status = ?
                where id = ?
                """;
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            fillOrderFields(statement, order);
            statement.setLong(7, order.getId());
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(long id) throws SQLException {
        String sql = "delete from orders where id = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
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
