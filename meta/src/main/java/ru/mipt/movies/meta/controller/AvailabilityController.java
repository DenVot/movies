package ru.mipt.movies.meta.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.mipt.movies.meta.repository.FilmMetadataRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/films")
public class AvailabilityController {

    private final FilmMetadataRepository repository;

    public AvailabilityController(FilmMetadataRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/{filmId}/availability")
    public ResponseEntity<Map<String, Boolean>> checkAvailability(@PathVariable String filmId) {
        try {
            UUID id = UUID.fromString(filmId);
            boolean isAvailable = repository.findById(id)
                    .map(film -> film.isAvailable())
                    .orElse(false);
            
            Map<String, Boolean> response = new HashMap<>();
            response.put("available", isAvailable);
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}

