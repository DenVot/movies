package ru.mipt.movies.meta.dto;

public class AvailabilityResponse {
    private Boolean available;

    public AvailabilityResponse() {
    }

    public AvailabilityResponse(Boolean available) {
        this.available = available;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }
}
