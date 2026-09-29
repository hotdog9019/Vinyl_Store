package ru.mirea.project.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
//инкапсуляция
public class Order {
    private long id;
    private long userId;
    private String albumTitle;
    private String artist;
    private int quantity;
    private BigDecimal totalPrice;
    private OrderStatus status;
    private LocalDateTime createdAt;

    public Order(
            long id,
            long userId,
            String albumTitle,
            String artist,
            int quantity,
            BigDecimal totalPrice,
            OrderStatus status,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.userId = userId;
        this.albumTitle = albumTitle;
        this.artist = artist;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.status = status;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public long getUserId() {
        return userId;
    }

    public String getAlbumTitle() {
        return albumTitle;
    }

    public String getArtist() {
        return artist;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "%d | user=%d | %s - %s | qty=%d | %s | %s | %s".formatted(
                id, userId, artist, albumTitle, quantity, totalPrice, status.getTitle(), createdAt
        );
    }
}
