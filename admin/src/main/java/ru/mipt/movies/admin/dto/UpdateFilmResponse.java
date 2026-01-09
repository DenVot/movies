package ru.mipt.movies.admin.dto;

public class UpdateFilmResponse {
    private String message;
    private String filmId;

    public UpdateFilmResponse() {
    }

    public UpdateFilmResponse(String message, String filmId) {
        this.message = message;
        this.filmId = filmId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFilmId() {
        return filmId;
    }

    public void setFilmId(String filmId) {
        this.filmId = filmId;
    }
}
