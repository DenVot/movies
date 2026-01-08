package ru.mipt.movies.meta.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.mipt.movies.meta.model.FilmMetadata;
import ru.mipt.movies.meta.repository.FilmMetadataRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/films")
public class AvailabilityController {

    private final FilmMetadataRepository repository;

    public AvailabilityController(FilmMetadataRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/{filmId}")
    public ResponseEntity<Map<String, Object>> getFilm(@PathVariable String filmId) {
        try {
            UUID id = UUID.fromString(filmId);
            return repository.findById(id)
                    .map(film -> {
                        Map<String, Object> response = new HashMap<>();
                        response.put("filmId", film.getId().toString());
                        response.put("name", film.getName());
                        response.put("description", film.getDescription());
                        response.put("available", film.isAvailable());
                        response.put("createdAt", film.getCreatedAt());
                        response.put("updatedAt", film.getUpdatedAt());
                        return ResponseEntity.ok(response);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{filmId}/availability")
    public ResponseEntity<Map<String, Boolean>> checkAvailability(@PathVariable String filmId) {
        try {
            UUID id = UUID.fromString(filmId);
            boolean isAvailable = repository.findById(id)
                    .map(FilmMetadata::isAvailable)
                    .orElse(false);

            Map<String, Boolean> response = new HashMap<>();
            response.put("available", isAvailable);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/available")
    public ResponseEntity<List<Map<String, Object>>> getAvailableFilms() {
        try {
            List<FilmMetadata> availableFilms = repository.findByIsAvailableTrue();

            List<Map<String, Object>> response = new ArrayList<>();
            for (FilmMetadata film : availableFilms) {
                Map<String, Object> filmData = new HashMap<>();
                filmData.put("filmId", film.getId().toString());
                filmData.put("name", film.getName());
                filmData.put("description", film.getDescription());
                filmData.put("available", film.isAvailable());
                filmData.put("createdAt", film.getCreatedAt());
                filmData.put("updatedAt", film.getUpdatedAt());
                response.add(filmData);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllFilms() {
        try {
            List<FilmMetadata> allFilms = repository.findAll();

            List<Map<String, Object>> response = new ArrayList<>();
            for (FilmMetadata film : allFilms) {
                Map<String, Object> filmData = new HashMap<>();
                filmData.put("filmId", film.getId().toString());
                filmData.put("name", film.getName());
                filmData.put("description", film.getDescription());
                filmData.put("available", film.isAvailable());
                filmData.put("createdAt", film.getCreatedAt());
                filmData.put("updatedAt", film.getUpdatedAt());
                response.add(filmData);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
