package ru.mipt.movies.admin.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class KafkaVideoProducer {

    private static final Logger logger = LoggerFactory.getLogger(KafkaVideoProducer.class);
    
    private final KafkaTemplate<String, String> kafkaTemplate;
    private static final String TOPIC = "raw-video";

    public KafkaVideoProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendFilmId(UUID filmId) {
        try {
            logger.info("Sending film ID to Kafka: {}", filmId);
            
            Message<String> message = MessageBuilder
                    .withPayload(filmId.toString())
                    .setHeader(KafkaHeaders.TOPIC, TOPIC)
                    .setHeader(KafkaHeaders.KEY, filmId.toString())
                    .build();
            
            kafkaTemplate.send(message);
            
            logger.info("Film ID sent to Kafka successfully: {}", filmId);
        } catch (Exception e) {
            logger.error("Error sending film ID to Kafka: {}", filmId, e);
            throw new RuntimeException("Failed to send film ID to Kafka", e);
        }
    }
}
