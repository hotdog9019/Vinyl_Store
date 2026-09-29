package ru.mirea.project.model;

public enum OrderStatus {
    NEW("Новый"),
    PAID("Оплачен"),
    SHIPPED("Отправлен"),
    COMPLETED("Завершен"),
    CANCELED("Отменен");

    private final String title;

    OrderStatus(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public static OrderStatus fromMenuValue(String value) {
        return switch (value) {
            case "1" -> NEW;
            case "2" -> PAID;
            case "3" -> SHIPPED;
            case "4" -> COMPLETED;
            case "5" -> CANCELED;
            default -> throw new IllegalArgumentException("Неизвестный статус заказа: " + value);
        };
    }

    public static OrderStatus fromDatabaseValue(String value) {
        if (value == null || value.isBlank()) {
            return NEW;
        }
        return switch (value.trim().toUpperCase()) {
            case "CREATED", "NEW" -> NEW;
            case "PAID" -> PAID;
            case "SHIPPED" -> SHIPPED;
            case "COMPLETED" -> COMPLETED;
            case "CANCELLED", "CANCELED" -> CANCELED;
            default -> throw new IllegalArgumentException("Неизвестный статус заказа в базе данных: " + value);
        };
    }
}
