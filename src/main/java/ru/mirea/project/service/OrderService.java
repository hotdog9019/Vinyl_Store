package ru.mirea.project.service;

import ru.mirea.project.exception.BusinessException;
import ru.mirea.project.exception.EntityNotFoundException;
import ru.mirea.project.model.Order;
import ru.mirea.project.model.OrderStatus;
import ru.mirea.project.repository.OrderRepository;
import ru.mirea.project.repository.UserRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public OrderService(OrderRepository orderRepository, UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    public long create(
            long userId,
            String albumTitle,
            String artist,
            int quantity,
            BigDecimal totalPrice,
            OrderStatus status
    ) throws SQLException {
        validate(userId, albumTitle, artist, quantity, totalPrice);
        Order order = new Order(0, userId, albumTitle.trim(), artist.trim(), quantity, totalPrice, status, LocalDateTime.now());
        return orderRepository.create(order);
    }

    public List<Order> findAll() throws SQLException {
        return orderRepository.findAll();
    }

    public Order findById(long id) throws SQLException {
        return orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Заказ с id " + id + " не найден."));
    }

    public List<Order> search(String query) throws SQLException {
        return searchByAlbumOrArtist(query);
    }

    public List<Order> searchByAlbum(String query) throws SQLException {
        validateSearch(query);
        return orderRepository.searchByAlbum(query.trim());
    }

    public List<Order> searchByArtist(String query) throws SQLException {
        validateSearch(query);
        return orderRepository.searchByArtist(query.trim());
    }

    public List<Order> searchByAlbumOrArtist(String query) throws SQLException {
        validateSearch(query);
        return orderRepository.searchByAlbumOrArtist(query.trim());
    }

    public List<Order> filterByStatus(OrderStatus status) throws SQLException {
        return orderRepository.findByStatus(status);
    }

    public List<Order> filterByUser(long userId) throws SQLException {
        if (userRepository.findById(userId).isEmpty()) {
            throw new EntityNotFoundException("Пользователь с id " + userId + " не найден.");
        }
        return orderRepository.findByUserId(userId);
    }

    public List<Order> sortByTotalPrice() throws SQLException {
        return orderRepository.findAll().stream()
                .sorted(Comparator.comparing(Order::getTotalPrice))
                .toList();
    }

    public List<Order> sortByDate() throws SQLException {
        return orderRepository.findAllOrderByDateDesc();
    }

    public Map<OrderStatus, Long> countByStatus() throws SQLException {
        return orderRepository.findAll().stream()
                .collect(Collectors.groupingBy(Order::getStatus, Collectors.counting()));
    }

    public long countOrders() throws SQLException {
        return orderRepository.findAll().size();
    }

    public BigDecimal calculateRevenue() throws SQLException {
        return orderRepository.findAll().stream()
                .filter(order -> order.getStatus() != OrderStatus.CANCELED)
                .map(Order::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calculateAverageOrderPrice() throws SQLException {
        List<Order> orders = orderRepository.findAll();
        if (orders.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal sum = orders.stream()
                .map(Order::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(orders.size()), 2, RoundingMode.HALF_UP);
    }

    public void update(
            long id,
            long userId,
            String albumTitle,
            String artist,
            int quantity,
            BigDecimal totalPrice,
            OrderStatus status
    ) throws SQLException {
        Order oldOrder = findById(id);
        if (oldOrder.getStatus() == OrderStatus.COMPLETED || oldOrder.getStatus() == OrderStatus.CANCELED) {
            throw new BusinessException("Завершенный или отмененный заказ нельзя изменить.");
        }
        if (oldOrder.getStatus() == OrderStatus.CANCELED && status == OrderStatus.PAID) {
            throw new BusinessException("Отмененный заказ нельзя перевести в оплаченный.");
        }
        validate(userId, albumTitle, artist, quantity, totalPrice);
        Order order = new Order(id, userId, albumTitle.trim(), artist.trim(), quantity, totalPrice, status, oldOrder.getCreatedAt());
        if (!orderRepository.update(order)) {
            throw new EntityNotFoundException("Заказ с id " + id + " не удалось обновить.");
        }
    }

    public void delete(long id) throws SQLException {
        if (!orderRepository.delete(id)) {
            throw new EntityNotFoundException("Заказ с id " + id + " не найден.");
        }
    }

    private void validateSearch(String query) {
        if (query == null || query.isBlank()) {
            throw new BusinessException("Строка поиска не может быть пустой.");
        }
    }

    private void validate(
            long userId,
            String albumTitle,
            String artist,
            int quantity,
            BigDecimal totalPrice
    ) throws SQLException {
        if (userRepository.findById(userId).isEmpty()) {
            throw new EntityNotFoundException("Пользователь с id " + userId + " не существует.");
        }
        if (albumTitle == null || albumTitle.isBlank()) {
            throw new BusinessException("Название пластинки обязательно.");
        }
        if (artist == null || artist.isBlank()) {
            throw new BusinessException("Исполнитель обязателен.");
        }
        if (quantity <= 0) {
            throw new BusinessException("Количество должно быть больше нуля.");
        }
        if (quantity > 50) {
            throw new BusinessException("Нельзя заказать больше 50 пластинок в одном заказе.");
        }
        if (totalPrice == null || totalPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Стоимость заказа должна быть больше нуля.");
        }
    }
}
