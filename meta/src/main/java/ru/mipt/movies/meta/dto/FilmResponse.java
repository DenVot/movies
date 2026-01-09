package ru.mipt.movies.meta.dto;

import java.time.LocalDateTime;

public class FilmResponse {
    private String filmId;
    private String name;
    private String description;
    private Boolean available;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public FilmResponse() {
    }

    public FilmResponse(String filmId, String name, String description, Boolean available, 
                       LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.filmId = filmId;
        this.name = name;
        this.description = description;
        this.available = available;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getFilmId() {
        return filmId;
    }

    public void setFilmId(String filmId) {
        this.filmId = filmId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
