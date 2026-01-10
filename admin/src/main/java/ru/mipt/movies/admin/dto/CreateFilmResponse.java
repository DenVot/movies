package ru.mipt.movies.admin.dto;

public class CreateFilmResponse {
    private String filmId;
    private String message;

    public CreateFilmResponse() {
    }

    public CreateFilmResponse(String filmId, String message) {
        this.filmId = filmId;
        this.message = message;
    }

    public String getFilmId() {
        return filmId;
    }

    public void setFilmId(String filmId) {
        this.filmId = filmId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
