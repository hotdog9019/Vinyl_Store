package ru.mirea.project.service;

import ru.mirea.project.exception.BusinessException;
import ru.mirea.project.exception.EntityNotFoundException;
import ru.mirea.project.model.Vinyl;
import ru.mirea.project.repository.VinylRepository;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class VinylService {
    private final VinylRepository vinylRepository;

    public VinylService(VinylRepository vinylRepository) {
        this.vinylRepository = vinylRepository;
    }

    public long create(String title, String artist, String genre, BigDecimal price, int stock) throws SQLException {
        validate(title, artist, genre, price, stock);
        validateUnique(title, artist, 0);
        return vinylRepository.create(new Vinyl(0, title.trim(), artist.trim(), cleanGenre(genre), price, stock));
    }

    public List<Vinyl> findAll() throws SQLException {
        return vinylRepository.findAll();
    }

    public void update(long id, String title, String artist, String genre, BigDecimal price, int stock) throws SQLException {
        validate(title, artist, genre, price, stock);
        validateUnique(title, artist, id);
        if (!vinylRepository.update(new Vinyl(id, title.trim(), artist.trim(), cleanGenre(genre), price, stock))) {
            throw new EntityNotFoundException("Пластинка с id " + id + " не найдена.");
        }
    }

    public void delete(long id) throws SQLException {
        if (!vinylRepository.delete(id)) {
            throw new EntityNotFoundException("Пластинка с id " + id + " не найдена.");
        }
    }

    private void validate(String title, String artist, String genre, BigDecimal price, int stock) {
        if (title == null || title.isBlank()) {
            throw new BusinessException("Название пластинки обязательно.");
        }
        if (title.trim().length() > 50) {
            throw new BusinessException("Название пластинки не должно быть длиннее 50 символов.");
        }
        if (artist == null || artist.isBlank()) {
            throw new BusinessException("Исполнитель обязателен.");
        }
        if (artist.trim().length() > 50) {
            throw new BusinessException("Имя исполнителя не должно быть длиннее 50 символов.");
        }
        if (genre != null && genre.trim().length() > 30) {
            throw new BusinessException("Название жанра не должно быть длиннее 30 символов.");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Цена пластинки должна быть больше нуля.");
        }
        if (price.compareTo(new BigDecimal("1000000")) > 0 || price.scale() > 2) {
            throw new BusinessException("Цена должна быть не больше 1 000 000 и содержать максимум 2 знака после запятой.");
        }
        if (stock < 0) {
            throw new BusinessException("Остаток пластинок не может быть отрицательным.");
        }
        if (stock > 100_000) {
            throw new BusinessException("Остаток пластинок не может превышать 100 000.");
        }
    }

    private void validateUnique(String title, String artist, long excludedId) throws SQLException {
        boolean duplicate = vinylRepository.findAll().stream()
                .anyMatch(vinyl -> vinyl.getId() != excludedId
                        && vinyl.getTitle().equalsIgnoreCase(title.trim())
                        && vinyl.getArtist().equalsIgnoreCase(artist.trim()));
        if (duplicate) {
            throw new BusinessException("Такая пластинка этого исполнителя уже есть в каталоге.");
        }
    }

    private String cleanGenre(String genre) {
        return genre == null ? "" : genre.trim();
    }
}
