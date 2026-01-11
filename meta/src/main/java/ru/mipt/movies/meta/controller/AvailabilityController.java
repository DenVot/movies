package ru.mipt.movies.meta.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.mipt.movies.meta.dto.AvailabilityResponse;
import ru.mipt.movies.meta.dto.FilmResponse;
import ru.mipt.movies.meta.model.FilmMetadata;
import ru.mipt.movies.meta.repository.FilmMetadataRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/films")
public class AvailabilityController {

    private final FilmMetadataRepository repository;

    public AvailabilityController(FilmMetadataRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/{filmId}")
    public ResponseEntity<FilmResponse> getFilm(@PathVariable String filmId) {
        try {
            UUID id = UUID.fromString(filmId);
            return repository.findById(id)
                    .map(film -> {
                        FilmResponse response = new FilmResponse(
                                film.getId().toString(),
                                film.getName(),
                                film.getDescription(),
                                film.isAvailable(),
                                film.getDuration(),
                                film.getCreatedAt(),
                                film.getUpdatedAt()
                        );
                        return ResponseEntity.ok(response);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{filmId}/availability")
    public ResponseEntity<AvailabilityResponse> checkAvailability(@PathVariable String filmId) {
        try {
            UUID id = UUID.fromString(filmId);
            boolean isAvailable = repository.findById(id)
                    .map(FilmMetadata::isAvailable)
                    .orElse(false);

            AvailabilityResponse response = new AvailabilityResponse(isAvailable);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/available")
    public ResponseEntity<List<FilmResponse>> getAvailableFilms() {
        try {
            List<FilmMetadata> availableFilms = repository.findByIsAvailableTrue();

            List<FilmResponse> response = new ArrayList<>();
            for (FilmMetadata film : availableFilms) {
                FilmResponse filmResponse = new FilmResponse(
                        film.getId().toString(),
                        film.getName(),
                        film.getDescription(),
                        film.isAvailable(),
                        film.getDuration(),
                        film.getCreatedAt(),
                        film.getUpdatedAt()
                );
                response.add(filmResponse);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<FilmResponse>> getAllFilms() {
        try {
            List<FilmMetadata> allFilms = repository.findAll();

            List<FilmResponse> response = new ArrayList<>();
            for (FilmMetadata film : allFilms) {
                FilmResponse filmResponse = new FilmResponse(
                        film.getId().toString(),
                        film.getName(),
                        film.getDescription(),
                        film.isAvailable(),
                        film.getDuration(),
                        film.getCreatedAt(),
                        film.getUpdatedAt()
                );
                response.add(filmResponse);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
