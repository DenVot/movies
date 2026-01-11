package ru.mipt.movies.admin.dto;

public class FilmDto {
    private String filmId;
    private String name;
    private String description;
    private Boolean available;
    private String createdAt;
    private String updatedAt;
    private Integer durationSeconds;

    public FilmDto() {
    }

    public FilmDto(String filmId, String name, String description, Boolean available, 
                   String createdAt, String updatedAt, Integer durationSeconds) {
        this.filmId = filmId;
        this.name = name;
        this.description = description;
        this.available = available;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.durationSeconds = durationSeconds;
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

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Integer durationSeconds) {
        this.durationSeconds = durationSeconds;
    }
}
