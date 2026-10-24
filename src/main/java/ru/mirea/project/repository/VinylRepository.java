package ru.mirea.project.repository;

import ru.mirea.project.db.DatabaseManager;
import ru.mirea.project.model.Vinyl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VinylRepository implements CrudRepository<Vinyl> {
    @Override
    public long create(Vinyl vinyl) throws SQLException {
        String sql = "insert into vinyl (title, artist, genre, price, stock) values (?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillFields(statement, vinyl);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
                throw new SQLException("База данных не вернула id пластинки.");
            }
        }
    }

    @Override
    public List<Vinyl> findAll() throws SQLException {
        String sql = "select id, title, artist, genre, price, stock from vinyl order by artist, title";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<Vinyl> vinyls = new ArrayList<>();
            while (resultSet.next()) {
                vinyls.add(map(resultSet));
            }
            return vinyls;
        }
    }

    @Override
    public Optional<Vinyl> findById(long id) throws SQLException {
        String sql = "select id, title, artist, genre, price, stock from vinyl where id = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(map(resultSet)) : Optional.empty();
            }
        }
    }

    @Override
    public boolean update(Vinyl vinyl) throws SQLException {
        String sql = "update vinyl set title = ?, artist = ?, genre = ?, price = ?, stock = ? where id = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            fillFields(statement, vinyl);
            statement.setLong(6, vinyl.getId());
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(long id) throws SQLException {
        String sql = "delete from vinyl where id = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    private void fillFields(PreparedStatement statement, Vinyl vinyl) throws SQLException {
        statement.setString(1, vinyl.getTitle());
        statement.setString(2, vinyl.getArtist());
        statement.setString(3, vinyl.getGenre());
        statement.setBigDecimal(4, vinyl.getPrice());
        statement.setInt(5, vinyl.getStock());
    }

    private Vinyl map(ResultSet resultSet) throws SQLException {
        return new Vinyl(
                resultSet.getLong("id"),
                resultSet.getString("title"),
                resultSet.getString("artist"),
                resultSet.getString("genre"),
                resultSet.getBigDecimal("price"),
                resultSet.getInt("stock")
        );
    }
}
