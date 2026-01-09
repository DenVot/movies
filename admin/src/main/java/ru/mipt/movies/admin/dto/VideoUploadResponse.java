package ru.mipt.movies.admin.dto;

public class VideoUploadResponse {
    private String message;
    private String filmId;
    private String objectName;
    private Long size;

    public VideoUploadResponse() {
    }

    public VideoUploadResponse(String message, String filmId, String objectName, Long size) {
        this.message = message;
        this.filmId = filmId;
        this.objectName = objectName;
        this.size = size;
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

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }
}
