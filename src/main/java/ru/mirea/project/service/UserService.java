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
        String normalizedEmail = normalizeEmail(email);
        String normalizedPhone = normalizePhone(phone);
        validateUniqueContacts(normalizedEmail, normalizedPhone, 0);
        return userRepository.create(new User(0, fullName.trim(), normalizedEmail, normalizedPhone));
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
        String normalizedEmail = normalizeEmail(email);
        String normalizedPhone = normalizePhone(phone);
        validateUniqueContacts(normalizedEmail, normalizedPhone, id);
        boolean updated = userRepository.update(new User(id, fullName.trim(), normalizedEmail, normalizedPhone));
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
        String normalizedName = fullName.trim();
        if (normalizedName.length() < 2 || normalizedName.length() > 50) {
            throw new BusinessException("ФИО должно содержать от 2 до 50 символов.");
        }
        if (!normalizedName.matches("[\\p{L} .'-]+")) {
            throw new BusinessException("ФИО может содержать только буквы, пробелы, точку, дефис и апостроф.");
        }
        if (email == null || email.isBlank()) {
            throw new BusinessException("Email обязателен.");
        }
        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail.length() > 120) {
            throw new BusinessException("Email не должен быть длиннее 120 символов.");
        }

        if (!normalizedEmail.matches(
                "^[a-z0-9._%+-]+@(yandex\\.ru|mail\\.ru)$"
        )) {
            throw new BusinessException(
                    "Разрешены только почтовые адреса @yandex.ru и @mail.ru."
            );
        }
        if (phone == null || phone.isBlank()) {
            throw new BusinessException("Телефон обязателен.");
        }
        String normalizedInput = phone.trim();
        if (!normalizedInput.matches("(?:\\+7|8)[\\d ()-]*")) {
            throw new BusinessException("Телефон должен начинаться с +7 или 8 и содержать только цифры, пробелы, скобки и дефисы.");
        }
        String digits = phone.replaceAll("\\D", "");
        if (digits.length() != 11 || (digits.charAt(0) != '7' && digits.charAt(0) != '8')) {
            throw new BusinessException("Телефон должен содержать 11 цифр и начинаться с +7 или 8.");
        }
    }

    private String normalizePhone(String phone) {
        String digits = phone.replaceAll("\\D", "");
        return "+7" + digits.substring(1);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private void validateUniqueContacts(String email, String phone, long excludedId) throws SQLException {
        if (userRepository.existsByEmailExceptId(email, excludedId)) {
            throw new BusinessException("Пользователь с таким email уже существует.");
        }
        if (userRepository.existsByPhoneExceptId(phone, excludedId)) {
            throw new BusinessException("Пользователь с таким телефоном уже существует.");
        }
    }
}
