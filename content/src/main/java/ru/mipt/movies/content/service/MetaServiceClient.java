package ru.mipt.movies.content.service;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.mipt.movies.meta.proto.MetaServiceGrpc;
import ru.mipt.movies.meta.proto.SetAvailabilityRequest;
import ru.mipt.movies.meta.proto.SetAvailabilityResponse;

import java.util.UUID;

@Service
public class MetaServiceClient {

    private static final Logger log = LoggerFactory.getLogger(MetaServiceClient.class);

    @GrpcClient("meta-service")
    private MetaServiceGrpc.MetaServiceBlockingStub metaServiceStub;

    public boolean setFilmAvailability(UUID filmId, boolean isAvailable) {
        try {
            log.info("Setting availability for film ID: {} to {}", filmId, isAvailable);
            
            SetAvailabilityRequest request = SetAvailabilityRequest.newBuilder()
                    .setFilmId(filmId.toString())
                    .setIsAvailable(isAvailable)
                    .build();
            
            SetAvailabilityResponse response = metaServiceStub.setAvailability(request);
            
            log.info("Availability set successfully for film ID: {}, success: {}", filmId, response.getSuccess());
            return response.getSuccess();
        } catch (Exception e) {
            log.error("Error setting availability for film ID: {}", filmId, e);
            throw new RuntimeException("Failed to set film availability in Meta Service", e);
        }
    }
}

