package ru.mirea.project.model;
//инк
public class User {
    private long id;
    private String fullName;
    private String email;
    private String phone;

    public User(long id, String fullName, String email, String phone) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
    }

    public long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    @Override
    public String toString() {
        return "%d | %s | %s | %s".formatted(id, fullName, email, phone);
    }
}
