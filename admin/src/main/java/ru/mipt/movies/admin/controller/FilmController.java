package ru.mipt.movies.admin.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.mipt.movies.admin.dto.*;
import ru.mipt.movies.admin.service.KafkaVideoProducer;
import ru.mipt.movies.admin.service.MetaServiceClient;
import ru.mipt.movies.admin.service.MinIOService;
import ru.mipt.movies.admin.service.VideoDurationExtractor;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/films")
public class FilmController {

    private static final Logger logger = LoggerFactory.getLogger(FilmController.class);

    private final MetaServiceClient metaServiceClient;
    private final KafkaVideoProducer kafkaVideoProducer;
    private final MinIOService minIOService;
    private final VideoDurationExtractor videoDurationExtractor;

    public FilmController(
            MetaServiceClient metaServiceClient,
            KafkaVideoProducer kafkaVideoProducer,
            MinIOService minIOService,
            VideoDurationExtractor videoDurationExtractor) {
        this.metaServiceClient = metaServiceClient;
        this.kafkaVideoProducer = kafkaVideoProducer;
        this.minIOService = minIOService;
        this.videoDurationExtractor = videoDurationExtractor;
    }

    @GetMapping
    public ResponseEntity<List<FilmDto>> getAllFilms() {
        try {
            List<FilmDto> films = metaServiceClient.getAllFilms();
            return ResponseEntity.ok(films);
        } catch (Exception e) {
            logger.error("Error getting all films", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping
    public ResponseEntity<CreateFilmResponse> createFilm(@RequestBody CreateFilmRequest request) {
        try {
            logger.info("Creating film: name={}, description={}", request.getName(), request.getDescription());

            UUID filmId = metaServiceClient.createFilm(request.getName(), request.getDescription());

            CreateFilmResponse response = new CreateFilmResponse(filmId.toString(), "Film created successfully");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            logger.error("Error creating film", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/{filmId}")
    public ResponseEntity<FilmDto> getFilm(@PathVariable String filmId) {
        try {
            logger.info("Getting film: filmId={}", filmId);

            UUID filmUuid = UUID.fromString(filmId);
            FilmDto film = metaServiceClient.getFilm(filmUuid);

            if (film == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            return ResponseEntity.ok(film);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid film ID format: {}", filmId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            logger.error("Error getting film: filmId={}", filmId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PutMapping("/{filmId}")
    public ResponseEntity<UpdateFilmResponse> updateFilm(
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
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            UpdateFilmResponse response = new UpdateFilmResponse("Film updated successfully", filmId);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid film ID format: {}", filmId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            logger.error("Error updating film: filmId={}", filmId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @DeleteMapping("/{filmId}")
    public ResponseEntity<DeleteFilmResponse> deleteFilm(@PathVariable String filmId) {
        try {
            logger.info("Deleting film: filmId={}", filmId);

            UUID filmUuid = UUID.fromString(filmId);
            boolean success = metaServiceClient.deleteFilm(filmUuid);

            if (!success) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            DeleteFilmResponse response = new DeleteFilmResponse("Film deleted successfully", filmId);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid film ID format: {}", filmId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            logger.error("Error deleting film: filmId={}", filmId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/{filmId}/video")
    public ResponseEntity<VideoUploadResponse> uploadVideo(
            @PathVariable String filmId,
            @RequestParam("file") MultipartFile file) {
        try {
            logger.info("Uploading video for film ID: {}, filename: {}, size: {} bytes",
                    filmId, file.getOriginalFilename(), file.getSize());

            validateVideoFile(file);

            UUID filmUuid = UUID.fromString(filmId);

            FilmDto film = metaServiceClient.getFilm(filmUuid);
            if (film == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            try {
                int durationSeconds = videoDurationExtractor.extractDuration(file);
                metaServiceClient.setDuration(filmUuid, durationSeconds);
                logger.info("Set video duration for film ID {}: {} seconds", filmId, durationSeconds);
            } catch (Exception e) {
                logger.warn("Failed to extract or set video duration for film ID {}", filmId, e);
            }

            String objectName = minIOService.uploadVideo(
                    filmUuid,
                    file.getInputStream(),
                    file.getSize(),
                    file.getContentType());

            try {
                kafkaVideoProducer.sendFilmId(filmUuid);
            } catch (Exception e) {
                logger.warn("Failed to send film ID to Kafka for film ID {}", filmId, e);
            }

            VideoUploadResponse response = new VideoUploadResponse(
                    "Video uploaded successfully",
                    filmId,
                    objectName,
                    file.getSize());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid film ID format: {}", filmId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            logger.error("Error uploading video for film ID: {}", filmId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PutMapping("/{filmId}/video")
    public ResponseEntity<VideoUploadResponse> replaceVideo(
            @PathVariable String filmId,
            @RequestParam("file") MultipartFile file) {
        try {
            logger.info("Replacing video for film ID: {}, filename: {}, size: {} bytes",
                    filmId, file.getOriginalFilename(), file.getSize());

            validateVideoFile(file);

            UUID filmUuid = UUID.fromString(filmId);

            FilmDto film = metaServiceClient.getFilm(filmUuid);
            if (film == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            try {
                int durationSeconds = videoDurationExtractor.extractDuration(file);
                metaServiceClient.setDuration(filmUuid, durationSeconds);
                logger.info("Set video duration for film ID {}: {} seconds", filmId, durationSeconds);
            } catch (Exception e) {
                logger.warn("Failed to extract or set video duration for film ID {}", filmId, e);
            }

            String objectName = minIOService.replaceVideo(
                    filmUuid,
                    file.getInputStream(),
                    file.getSize(),
                    file.getContentType());

            try {
                kafkaVideoProducer.sendFilmId(filmUuid);
            } catch (Exception e) {
                logger.warn("Failed to send film ID to Kafka for film ID {}", filmId, e);
            }

            VideoUploadResponse response = new VideoUploadResponse(
                    "Video replaced successfully",
                    filmId,
                    objectName,
                    file.getSize());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid film ID format: {}", filmId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            logger.error("Error replacing video for film ID: {}", filmId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @DeleteMapping("/{filmId}/video")
    public ResponseEntity<DeleteVideoResponse> deleteVideo(@PathVariable String filmId) {
        try {
            logger.info("Deleting video for film ID: {}", filmId);

            UUID filmUuid = UUID.fromString(filmId);

            FilmDto film = metaServiceClient.getFilm(filmUuid);
            if (film == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            minIOService.deleteVideo(filmUuid);

            DeleteVideoResponse response = new DeleteVideoResponse("Video deleted successfully", filmId);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid film ID format: {}", filmId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            logger.error("Error deleting video for film ID: {}", filmId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
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
