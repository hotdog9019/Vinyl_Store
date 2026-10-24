package ru.mirea.project.service;

import org.junit.jupiter.api.Test;
import ru.mirea.project.exception.BusinessException;
import ru.mirea.project.model.User;
import ru.mirea.project.model.Vinyl;
import ru.mirea.project.repository.UserRepository;
import ru.mirea.project.repository.VinylRepository;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InputValidationTest {
    @Test
    void userValidationRejectsUnsupportedEmailAndPhone() {
        UserService service = new UserService(new StubUserRepository());

        assertThatThrownBy(() -> service.create("Иван Иванов", "ivan@gmail.com", "+79991234567"))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> service.create("Иван Иванов", "ivan@mail.ru", "79991234567"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void userValidationNormalizesAllowedContacts() throws SQLException {
        StubUserRepository repository = new StubUserRepository();
        UserService service = new UserService(repository);

        service.create("Иван Иванов", "IVAN@YANDEX.RU", "8 (999) 123-45-67");

        assertThat(repository.created.getEmail()).isEqualTo("ivan@yandex.ru");
        assertThat(repository.created.getPhone()).isEqualTo("+79991234567");
    }

    @Test
    void vinylValidationChecksLengthsPriceStockAndDuplicates() {
        StubVinylRepository repository = new StubVinylRepository();
        VinylService service = new VinylService(repository);

        assertThatThrownBy(() -> service.create("A".repeat(51), "Artist", "Rock", new BigDecimal("100"), 1))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> service.create("Album", "Artist", "Rock", new BigDecimal("100.123"), 1))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> service.create("Album", "Artist", "Rock", new BigDecimal("100"), 100_001))
                .isInstanceOf(BusinessException.class);

        repository.vinyls.add(new Vinyl(1, "Album", "Artist", "Rock", new BigDecimal("100"), 1));
        assertThatThrownBy(() -> service.create("album", "artist", "Rock", new BigDecimal("100"), 1))
                .isInstanceOf(BusinessException.class);
    }

    private static class StubUserRepository extends UserRepository {
        private User created;

        @Override
        public long create(User user) {
            created = user;
            return 1;
        }

        @Override
        public boolean existsByEmailExceptId(String email, long excludedId) {
            return false;
        }

        @Override
        public boolean existsByPhoneExceptId(String phone, long excludedId) {
            return false;
        }
    }

    private static class StubVinylRepository extends VinylRepository {
        private final List<Vinyl> vinyls = new ArrayList<>();

        @Override
        public List<Vinyl> findAll() {
            return vinyls;
        }
    }
}
