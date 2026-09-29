package ru.mirea.project.service;

import ru.mirea.project.exception.BusinessException;
import ru.mirea.project.exception.EntityNotFoundException;
import ru.mirea.project.model.User;
import ru.mirea.project.repository.UserRepository;

import java.sql.SQLException;
import java.util.List;

public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public long create(String fullName, String email, String phone) throws SQLException {
        validate(fullName, email, phone);
        return userRepository.create(new User(0, fullName.trim(), email.trim(), phone.trim()));
    }

    public List<User> findAll() throws SQLException {
        return userRepository.findAll();
    }

    public User findById(long id) throws SQLException {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь с id " + id + " не найден."));
    }

    public void update(long id, String fullName, String email, String phone) throws SQLException {
        findById(id);
        validate(fullName, email, phone);
        boolean updated = userRepository.update(new User(id, fullName.trim(), email.trim(), phone.trim()));
        if (!updated) {
            throw new EntityNotFoundException("Пользователь с id " + id + " не найден.");
        }
    }

    public void delete(long id) throws SQLException {
        boolean deleted = userRepository.delete(id);
        if (!deleted) {
            throw new EntityNotFoundException("Пользователь с id " + id + " не найден.");
        }
    }

    public long countUsers() throws SQLException {
        return userRepository.findAll().size();
    }

    private void validate(String fullName, String email, String phone) {
        if (fullName == null || fullName.isBlank()) {
            throw new BusinessException("Имя пользователя обязательно.");
        }
        if (email == null || email.isBlank()) {
            throw new BusinessException("Email обязателен.");
        }
        if (!email.contains("@")) {
            throw new BusinessException("Email должен содержать символ @.");
        }
        if (phone == null || phone.isBlank()) {
            throw new BusinessException("Телефон обязателен.");
        }
    }
}
