package ru.mirea.project.repository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface CrudRepository<T> {
    long create(T entity) throws SQLException;

    List<T> findAll() throws SQLException;

    Optional<T> findById(long id) throws SQLException;

    boolean update(T entity) throws SQLException;

    boolean delete(long id) throws SQLException;
}