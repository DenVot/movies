package ru.mipt.movies.admin.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.mipt.movies.admin.service.KafkaVideoProducer;
import ru.mipt.movies.admin.service.MetaServiceClient;
import ru.mipt.movies.admin.service.MinIOService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/films")
public class FilmController {

    private static final Logger logger = LoggerFactory.getLogger(FilmController.class);

    private final MetaServiceClient metaServiceClient;
    private final KafkaVideoProducer kafkaVideoProducer;
    private final MinIOService minIOService;

    public FilmController(
            MetaServiceClient metaServiceClient,
            KafkaVideoProducer kafkaVideoProducer,
            MinIOService minIOService
    ) {
        this.metaServiceClient = metaServiceClient;
        this.kafkaVideoProducer = kafkaVideoProducer;
        this.minIOService = minIOService;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllFilms() {
        try {
            List<Map<String, Object>> films = metaServiceClient.getAllFilms();

            return ResponseEntity.ok(films);
        } catch (Exception e) {
            logger.error("Error getting all films", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to get films: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(List.of(errorResponse));
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createFilm(@RequestBody CreateFilmRequest request) {
        try {
            logger.info("Creating film: name={}, description={}", request.getName(), request.getDescription());

            UUID filmId = metaServiceClient.createFilm(request.getName(), request.getDescription());

            Map<String, Object> response = new HashMap<>();
            response.put("filmId", filmId.toString());
            response.put("message", "Film created successfully");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            logger.error("Error creating film", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to create film: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/{filmId}")
    public ResponseEntity<Map<String, Object>> getFilm(@PathVariable String filmId) {
        try {
            logger.info("Getting film: filmId={}", filmId);

            UUID filmUuid = UUID.fromString(filmId);
            Map<String, Object> film = metaServiceClient.getFilm(filmUuid);

            if (film == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Film not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            return ResponseEntity.ok(film);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid film ID format: {}", filmId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid film ID format");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            logger.error("Error getting film: filmId={}", filmId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to get film: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PutMapping("/{filmId}")
    public ResponseEntity<Map<String, Object>> updateFilm(
            @PathVariable String filmId,
            @RequestBody UpdateFilmRequest request) {
        try {
            logger.info("Updating film: filmId={}, name={}, description={}",
                    filmId, request.getName(), request.getDescription());

            UUID filmUuid = UUID.fromString(filmId);
            boolean success = metaServiceClient.updateFilm(
                    filmUuid,
                    request.getName(),
                    request.getDescription());

            if (!success) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Film not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Film updated successfully");
            response.put("filmId", filmId);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid film ID format: {}", filmId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid film ID format");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            logger.error("Error updating film: filmId={}", filmId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to update film: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @DeleteMapping("/{filmId}")
    public ResponseEntity<Map<String, Object>> deleteFilm(@PathVariable String filmId) {
        try {
            logger.info("Deleting film: filmId={}", filmId);

            UUID filmUuid = UUID.fromString(filmId);
            boolean success = metaServiceClient.deleteFilm(filmUuid);

            if (!success) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Film not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Film deleted successfully");
            response.put("filmId", filmId);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid film ID format: {}", filmId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid film ID format");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            logger.error("Error deleting film: filmId={}", filmId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to delete film: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/{filmId}/video")
    public ResponseEntity<Map<String, Object>> uploadVideo(
            @PathVariable String filmId,
            @RequestParam("file") MultipartFile file) {
        try {
            logger.info("Uploading video for film ID: {}, filename: {}, size: {} bytes",
                    filmId, file.getOriginalFilename(), file.getSize());

            validateVideoFile(file);

            UUID filmUuid = UUID.fromString(filmId);

            Map<String, Object> film = metaServiceClient.getFilm(filmUuid);
            if (film == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Film not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            String objectName = minIOService.uploadVideo(
                    filmUuid, 
                    file.getInputStream(), 
                    file.getSize(), 
                    file.getContentType()
            );
            
            kafkaVideoProducer.sendFilmId(filmUuid);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Video uploaded successfully");
            response.put("filmId", filmId);
            response.put("objectName", objectName);
            response.put("size", file.getSize());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid film ID format: {}", filmId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid film ID format");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            logger.error("Error uploading video for film ID: {}", filmId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to upload video: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PutMapping("/{filmId}/video")
    public ResponseEntity<Map<String, Object>> replaceVideo(
            @PathVariable String filmId,
            @RequestParam("file") MultipartFile file) {
        try {
            logger.info("Replacing video for film ID: {}, filename: {}, size: {} bytes",
                    filmId, file.getOriginalFilename(), file.getSize());

            validateVideoFile(file);

            UUID filmUuid = UUID.fromString(filmId);

            Map<String, Object> film = metaServiceClient.getFilm(filmUuid);
            if (film == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Film not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            String objectName = minIOService.replaceVideo(
                    filmUuid,
                    file.getInputStream(),
                    file.getSize(),
                    file.getContentType());

            kafkaVideoProducer.sendFilmId(filmUuid);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Video replaced successfully");
            response.put("filmId", filmId);
            response.put("objectName", objectName);
            response.put("size", file.getSize());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid film ID format: {}", filmId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid film ID format");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            logger.error("Error replacing video for film ID: {}", filmId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to replace video: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @DeleteMapping("/{filmId}/video")
    public ResponseEntity<Map<String, Object>> deleteVideo(@PathVariable String filmId) {
        try {
            logger.info("Deleting video for film ID: {}", filmId);

            UUID filmUuid = UUID.fromString(filmId);

            Map<String, Object> film = metaServiceClient.getFilm(filmUuid);
            if (film == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Film not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            minIOService.deleteVideo(filmUuid);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Video deleted successfully");
            response.put("filmId", filmId);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid film ID format: {}", filmId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid film ID format");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            logger.error("Error deleting video for film ID: {}", filmId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to delete video: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


    private void validateVideoFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("video/mp4")) {
            throw new IllegalArgumentException("Only MP4 files are supported");
        }
    }

    public static class CreateFilmRequest {
        private String name;
        private String description;

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
    }

    public static class UpdateFilmRequest {
        private String name;
        private String description;

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
    }
}
