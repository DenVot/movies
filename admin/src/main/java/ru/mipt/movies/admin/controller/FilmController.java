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
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/films")
public class FilmController {

    private static final Logger logger = LoggerFactory.getLogger(FilmController.class);

    private final MetaServiceClient metaServiceClient;
    private final KafkaVideoProducer kafkaVideoProducer;
    private final MinIOService minIOService;

    public FilmController(MetaServiceClient metaServiceClient, KafkaVideoProducer kafkaVideoProducer, MinIOService minIOService) {
        this.metaServiceClient = metaServiceClient;
        this.kafkaVideoProducer = kafkaVideoProducer;
        this.minIOService = minIOService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> createFilm(@RequestBody CreateFilmRequest request) {
        try {
            logger.info("Creating film: name={}, description={}", request.getName(), request.getDescription());
            
            UUID filmId = metaServiceClient.createFilm(request.getName(), request.getDescription());
            
            Map<String, String> response = new HashMap<>();
            response.put("filmId", filmId.toString());
            response.put("message", "Film created successfully");
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            logger.error("Error creating film", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to create film: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/{filmId}/upload")
    public ResponseEntity<Map<String, String>> uploadVideo(
            @PathVariable String filmId,
            @RequestParam("file") MultipartFile file) {
        try {
            logger.info("Uploading video for film ID: {}, filename: {}, size: {} bytes", 
                    filmId, file.getOriginalFilename(), file.getSize());
            
            if (file.isEmpty()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "File is empty");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
            
            String contentType = file.getContentType();
            if (contentType == null || !contentType.equals("video/mp4")) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Only MP4 files are supported");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
            
            UUID filmUuid = UUID.fromString(filmId);
            
            String objectName = minIOService.uploadVideo(
                    filmUuid, 
                    file.getInputStream(), 
                    file.getSize(), 
                    file.getContentType()
            );
            
            kafkaVideoProducer.sendFilmId(filmUuid);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Video uploaded successfully");
            response.put("filmId", filmId);
            response.put("objectName", objectName);
            response.put("size", String.valueOf(file.getSize()));
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid film ID format: {}", filmId, e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid film ID format");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            logger.error("Error uploading video for film ID: {}", filmId, e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to upload video: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
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
}
