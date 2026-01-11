package ru.mipt.movies.content.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.mipt.movies.content.service.MinIOService;
import ru.mipt.movies.content.service.VideoProcessingService;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/videos")
public class VideoStreamController {

    private static final Logger log = LoggerFactory.getLogger(VideoStreamController.class);
    private final MinIOService minIOService;
    private final VideoProcessingService videoProcessingService;

    public VideoStreamController(
            MinIOService minIOService,
            VideoProcessingService videoProcessingService) {
        this.minIOService = minIOService;
        this.videoProcessingService = videoProcessingService;
    }

    @GetMapping("/{filmId}/stream")
    public ResponseEntity<InputStreamResource> streamVideo(
            @PathVariable String filmId,
            @RequestParam(value = "quality", required = false) String quality,
            @RequestHeader(value = HttpHeaders.RANGE, required = false) String rangeHeader) {
        try {
            UUID filmUuid = UUID.fromString(filmId);

            if (quality == null || quality.isEmpty()) {
                quality = "raw";
            }

            if (!quality.equals("raw")) {
                List<String> availableQualities = videoProcessingService.getAvailableQualities();
                if (!availableQualities.contains(quality)) {
                    log.warn("Invalid quality requested: {} for film ID: {}. Available qualities: {}",
                            quality, filmId, availableQualities);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(null);
                }
            }

            log.info("Streaming video for film ID: {}, quality: {}, range header: {}", filmId, quality, rangeHeader);

            long videoSize;
            if (quality.equals("raw")) {
                videoSize = minIOService.getRawVideoSize(filmUuid);
            } else {
                videoSize = minIOService.getVideoSize(filmUuid, quality);
            }
            long rangeStart = 0;
            long rangeEnd = videoSize - 1;
            long contentLength = videoSize;

            if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
                String[] ranges = rangeHeader.substring(6).split("-");
                try {
                    if (ranges[0].length() > 0) {
                        rangeStart = Long.parseLong(ranges[0]);
                    }
                    if (ranges.length > 1 && ranges[1].length() > 0) {
                        rangeEnd = Long.parseLong(ranges[1]);
                    }
                } catch (NumberFormatException e) {
                    log.warn("Invalid range header: {}", rangeHeader);
                }
            }

            if (rangeStart < 0) {
                rangeStart = 0;
            }
            if (rangeEnd >= videoSize) {
                rangeEnd = videoSize - 1;
            }
            if (rangeStart > rangeEnd) {
                return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE).build();
            }

            contentLength = rangeEnd - rangeStart + 1;

            InputStream videoStream;
            if (quality.equals("raw")) {
                if (rangeStart == 0 && rangeEnd == videoSize - 1) {
                    videoStream = minIOService.getRawVideoStream(filmUuid);
                } else {
                    videoStream = minIOService.getRawVideoStreamRange(filmUuid, rangeStart, contentLength);
                }
            } else {
                if (rangeStart == 0 && rangeEnd == videoSize - 1) {
                    videoStream = minIOService.getVideoStream(filmUuid, quality);
                } else {
                    videoStream = minIOService.getVideoStreamRange(filmUuid, quality, rangeStart, contentLength);
                }
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentLength(contentLength);
            headers.set("Accept-Ranges", "bytes");

            if (rangeHeader != null) {
                headers.set(HttpHeaders.CONTENT_RANGE,
                        String.format("bytes %d-%d/%d", rangeStart, rangeEnd, videoSize));
                headers.set(HttpHeaders.CONTENT_LENGTH, String.valueOf(contentLength));

                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                        .headers(headers)
                        .body(new InputStreamResource(videoStream));
            } else {
                headers.set(HttpHeaders.CONTENT_LENGTH, String.valueOf(videoSize));
                return ResponseEntity.ok()
                        .headers(headers)
                        .body(new InputStreamResource(videoStream));
            }
        } catch (IllegalArgumentException e) {
            log.error("Invalid film ID format: {}", filmId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error streaming video for film ID: {}, quality: {}", filmId, quality, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{filmId}/qualities")
    public ResponseEntity<List<String>> getAvailableQualities(@PathVariable String filmId) {
        try {
            UUID.fromString(filmId);
            List<String> qualities = videoProcessingService.getAvailableQualities();
            return ResponseEntity.ok(qualities);
        } catch (IllegalArgumentException e) {
            log.error("Invalid film ID format: {}", filmId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
