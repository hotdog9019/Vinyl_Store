package ru.mirea.project.repository;

import ru.mirea.project.db.DatabaseManager;
import ru.mirea.project.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepository implements CrudRepository<User> {
    @Override
    public long create(User user) throws SQLException {
        try (Connection connection = DatabaseManager.getConnection()) {
            String sql = hasNameColumn(connection)
                    ? "insert into users (name, full_name, email, phone) values (?, ?, ?, ?)"
                    : "insert into users (full_name, email, phone) values (?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, user.getFullName());
                if (hasNameColumn(connection)) {
                    statement.setString(2, user.getFullName());
                    statement.setString(3, user.getEmail());
                    statement.setString(4, user.getPhone());
                } else {
                    statement.setString(2, user.getEmail());
                    statement.setString(3, user.getPhone());
                }
                statement.executeUpdate();
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (keys.next()) {
                        return keys.getLong(1);
                    }
                    throw new SQLException("База данных не вернула id пользователя.");
                }
            }
        }
    }

    @Override
    public List<User> findAll() throws SQLException {
        String sql = "select id, full_name, email, phone from users order by id";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<User> users = new ArrayList<>();
            while (resultSet.next()) {
                users.add(mapUser(resultSet));
            }
            return users;
        }
    }

    @Override
    public Optional<User> findById(long id) throws SQLException {
        String sql = "select id, full_name, email, phone from users where id = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapUser(resultSet));
                }
                return Optional.empty();
            }
        }
    }

    @Override
    public boolean update(User user) throws SQLException {
        try (Connection connection = DatabaseManager.getConnection()) {
            String sql = hasNameColumn(connection)
                    ? "update users set name = ?, full_name = ?, email = ?, phone = ? where id = ?"
                    : "update users set full_name = ?, email = ?, phone = ? where id = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, user.getFullName());
                if (hasNameColumn(connection)) {
                    statement.setString(2, user.getFullName());
                    statement.setString(3, user.getEmail());
                    statement.setString(4, user.getPhone());
                    statement.setLong(5, user.getId());
                } else {
                    statement.setString(2, user.getEmail());
                    statement.setString(3, user.getPhone());
                    statement.setLong(4, user.getId());
                }
                return statement.executeUpdate() > 0;
            }
        }
    }

    @Override
    public boolean delete(long id) throws SQLException {
        String sql = "delete from users where id = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    private boolean hasNameColumn(Connection connection) throws SQLException {
        String sql = """
                select 1
                from information_schema.columns
                where table_schema = 'public'
                  and table_name = 'users'
                  and column_name = 'name'
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next();
        }
    }

    private User mapUser(ResultSet resultSet) throws SQLException {
        return new User(
                resultSet.getLong("id"),
                resultSet.getString("full_name"),
                resultSet.getString("email"),
                resultSet.getString("phone")
        );
    }
}
