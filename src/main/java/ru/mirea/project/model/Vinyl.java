package ru.mirea.project.model;

import java.math.BigDecimal;

public class Vinyl {
    private final long id;
    private final String title;
    private final String artist;
    private final String genre;
    private final BigDecimal price;
    private final int stock;

    public Vinyl(long id, String title, String artist, String genre, BigDecimal price, int stock) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.genre = genre;
        this.price = price;
        this.stock = stock;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getGenre() {
        return genre;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }
}
