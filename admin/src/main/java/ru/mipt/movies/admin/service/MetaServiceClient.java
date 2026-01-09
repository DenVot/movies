package ru.mipt.movies.admin.service;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.mipt.movies.admin.dto.FilmDto;
import ru.mipt.movies.meta.proto.MetaServiceGrpc;
import ru.mipt.movies.meta.proto.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

    public FilmDto getFilm(UUID filmId) {
        try {
            logger.info("Getting film from Meta Service: filmId={}", filmId);

            GetFilmRequest request = GetFilmRequest.newBuilder()
                    .setFilmId(filmId.toString())
                    .build();

            GetFilmResponse response = metaServiceStub.getFilm(request);

            if (!response.getFound()) {
                logger.warn("Film not found: filmId={}", filmId);
                return null;
            }

            Film film = response.getFilm();
            FilmDto filmDto = new FilmDto(
                    film.getFilmId(),
                    film.getName(),
                    film.getDescription(),
                    film.getAvailable(),
                    film.getCreatedAt(),
                    film.getUpdatedAt()
            );

            logger.info("Film retrieved successfully: filmId={}", filmId);
            return filmDto;
        } catch (Exception e) {
            logger.error("Error getting film from Meta Service: filmId={}", filmId, e);
            throw new RuntimeException("Failed to get film from Meta Service", e);
        }
    }

    public boolean updateFilm(UUID filmId, String name, String description) {
        try {
            logger.info("Updating film in Meta Service: filmId={}, name={}, description={}", filmId, name, description);

            UpdateFilmRequest.Builder requestBuilder = UpdateFilmRequest.newBuilder()
                    .setFilmId(filmId.toString());

            if (name != null && !name.isEmpty()) {
                requestBuilder.setName(name);
            }
            if (description != null && !description.isEmpty()) {
                requestBuilder.setDescription(description);
            }

            UpdateFilmResponse response = metaServiceStub.updateFilm(requestBuilder.build());

            logger.info("Film update result: filmId={}, success={}", filmId, response.getSuccess());
            return response.getSuccess();
        } catch (Exception e) {
            logger.error("Error updating film in Meta Service: filmId={}", filmId, e);
            throw new RuntimeException("Failed to update film in Meta Service", e);
        }
    }

    public boolean deleteFilm(UUID filmId) {
        try {
            logger.info("Deleting film from Meta Service: filmId={}", filmId);

            DeleteFilmRequest request = DeleteFilmRequest.newBuilder()
                    .setFilmId(filmId.toString())
                    .build();

            DeleteFilmResponse response = metaServiceStub.deleteFilm(request);

            logger.info("Film deletion result: filmId={}, success={}", filmId, response.getSuccess());
            return response.getSuccess();
        } catch (Exception e) {
            logger.error("Error deleting film from Meta Service: filmId={}", filmId, e);
            throw new RuntimeException("Failed to delete film from Meta Service", e);
        }
    }

    public List<FilmDto> getAllFilms() {
        try {
            logger.info("Getting all films from Meta Service");

            GetAllFilmsRequest request = GetAllFilmsRequest.newBuilder().build();
            GetAllFilmsResponse response = metaServiceStub.getAllFilms(request);

            List<FilmDto> films = response.getFilmsList().stream()
                    .map(film -> new FilmDto(
                            film.getFilmId(),
                            film.getName(),
                            film.getDescription(),
                            film.getAvailable(),
                            film.getCreatedAt(),
                            film.getUpdatedAt()
                    ))
                    .collect(Collectors.toList());

            logger.info("Retrieved {} films successfully", films.size());
            return films;
        } catch (Exception e) {
            logger.error("Error getting all films from Meta Service", e);
            throw new RuntimeException("Failed to get all films from Meta Service", e);
        }
    }
}
