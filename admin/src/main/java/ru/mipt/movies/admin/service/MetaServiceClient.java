package ru.mipt.movies.admin.service;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.mipt.movies.meta.proto.MetaServiceGrpc;
import ru.mipt.movies.meta.proto.CreateFilmRequest;
import ru.mipt.movies.meta.proto.CreateFilmResponse;

import java.util.UUID;

@Service
public class MetaServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(MetaServiceClient.class);

    @GrpcClient("meta-service")
    private MetaServiceGrpc.MetaServiceBlockingStub metaServiceStub;

    public UUID createFilm(String name, String description) {
        try {
            logger.info("Creating film in Meta Service: name={}, description={}", name, description);
            
            CreateFilmRequest request = CreateFilmRequest.newBuilder()
                    .setName(name)
                    .setDescription(description)
                    .build();
            
            CreateFilmResponse response = metaServiceStub.createFilm(request);
            UUID filmId = UUID.fromString(response.getFilmId());
            
            logger.info("Film created successfully with ID: {}", filmId);
            return filmId;
        } catch (Exception e) {
            logger.error("Error creating film in Meta Service", e);
            throw new RuntimeException("Failed to create film in Meta Service", e);
        }
    }
}
