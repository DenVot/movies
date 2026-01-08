package ru.mipt.movies.content.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.mipt.movies.content.service.MetaServiceClient;
import ru.mipt.movies.content.service.VideoProcessingService;

import java.util.UUID;

@Component
public class VideoConsumer {

    private static final Logger log = LoggerFactory.getLogger(VideoConsumer.class);
    private final MetaServiceClient metaServiceClient;
    private final VideoProcessingService videoProcessingService;

    public VideoConsumer(MetaServiceClient metaServiceClient, VideoProcessingService videoProcessingService) {
        this.metaServiceClient = metaServiceClient;
        this.videoProcessingService = videoProcessingService;
    }

    @KafkaListener(topics = "raw-video", groupId = "content-service-group")
    public void consumeFilmId(String filmIdString) {
        try {
            log.info("Received film ID from Kafka: {}", filmIdString);

            UUID filmId = UUID.fromString(filmIdString);

            log.info("Processing video for film ID: {}", filmId);
            videoProcessingService.processVideo(filmId);

            log.info("Setting film {} as available for streaming", filmId);
            boolean success = metaServiceClient.setFilmAvailability(filmId, true);

            if (success) {
                log.info("Successfully set film {} as available for streaming", filmId);
            } else {
                log.warn("Failed to set film {} as available for streaming", filmId);
            }
        } catch (IllegalArgumentException e) {
            log.error("Invalid film ID format received from Kafka: {}", filmIdString, e);
        } catch (Exception e) {
            log.error("Error processing film ID from Kafka: {}", filmIdString, e);
        }
    }
}
