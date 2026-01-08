package ru.mipt.movies.meta.service;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mipt.movies.meta.model.FilmMetadata;
import ru.mipt.movies.meta.proto.MetaServiceGrpc;
import ru.mipt.movies.meta.proto.*;
import ru.mipt.movies.meta.repository.FilmMetadataRepository;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@GrpcService
public class MetaGrpcService extends MetaServiceGrpc.MetaServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(MetaGrpcService.class);

    private final FilmMetadataRepository repository;

    public MetaGrpcService(FilmMetadataRepository repository) {
        this.repository = repository;
    }

    @Override
    public void createFilm(CreateFilmRequest request, StreamObserver<CreateFilmResponse> responseObserver) {
        try {
            logger.info("Creating film with name: {}", request.getName());
            
            FilmMetadata film = new FilmMetadata();
            film.setName(request.getName());
            film.setDescription(request.getDescription());
            film.setAvailable(false);
            
            FilmMetadata savedFilm = repository.save(film);
            UUID filmId = savedFilm.getId();
            
            CreateFilmResponse response = CreateFilmResponse.newBuilder()
                    .setFilmId(filmId.toString())
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
            logger.info("Film created successfully with ID: {}", filmId);
        } catch (Exception e) {
            logger.error("Error creating film", e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void deleteFilm(DeleteFilmRequest request, StreamObserver<DeleteFilmResponse> responseObserver) {
        try {
            UUID filmId = UUID.fromString(request.getFilmId());
            logger.info("Deleting film with ID: {}", filmId);
            
            boolean exists = repository.existsById(filmId);
            if (exists) {
                repository.deleteById(filmId);
            }
            
            DeleteFilmResponse response = DeleteFilmResponse.newBuilder()
                    .setSuccess(exists)
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
            logger.info("Film deletion result: {}", exists);
        } catch (Exception e) {
            logger.error("Error deleting film", e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void updateFilm(UpdateFilmRequest request, StreamObserver<UpdateFilmResponse> responseObserver) {
        try {
            UUID filmId = UUID.fromString(request.getFilmId());
            logger.info("Updating film with ID: {}", filmId);
            
            FilmMetadata film = repository.findById(filmId).orElse(null);
            if (film == null) {
                UpdateFilmResponse response = UpdateFilmResponse.newBuilder()
                        .setSuccess(false)
                        .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
                logger.warn("Film with ID {} not found", filmId);
                return;
            }
            
            if (request.getName() != null && !request.getName().isEmpty()) {
                film.setName(request.getName());
            }
            if (request.getDescription() != null && !request.getDescription().isEmpty()) {
                film.setDescription(request.getDescription());
            }
            
            repository.save(film);
            
            UpdateFilmResponse response = UpdateFilmResponse.newBuilder()
                    .setSuccess(true)
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
            logger.info("Film update result: true");
        } catch (Exception e) {
            logger.error("Error updating film", e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void setAvailability(SetAvailabilityRequest request, StreamObserver<SetAvailabilityResponse> responseObserver) {
        try {
            UUID filmId = UUID.fromString(request.getFilmId());
            logger.info("Setting availability for film ID: {} to {}", filmId, request.getIsAvailable());
            
            FilmMetadata film = repository.findById(filmId).orElse(null);
            if (film == null) {
                SetAvailabilityResponse response = SetAvailabilityResponse.newBuilder()
                        .setSuccess(false)
                        .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
                logger.warn("Film with ID {} not found", filmId);
                return;
            }
            
            film.setAvailable(request.getIsAvailable());
            repository.save(film);
            
            SetAvailabilityResponse response = SetAvailabilityResponse.newBuilder()
                    .setSuccess(true)
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
            logger.info("Availability update result: true");
        } catch (Exception e) {
            logger.error("Error setting availability", e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void getFilm(GetFilmRequest request, StreamObserver<GetFilmResponse> responseObserver) {
        try {
            UUID filmId = UUID.fromString(request.getFilmId());
            logger.info("Getting film with ID: {}", filmId);

            FilmMetadata film = repository.findById(filmId).orElse(null);
            if (film == null) {
                GetFilmResponse response = GetFilmResponse.newBuilder()
                        .setFound(false)
                        .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
                logger.warn("Film with ID {} not found", filmId);
                return;
            }

            Film filmProto = convertToFilmProto(film);
            GetFilmResponse response = GetFilmResponse.newBuilder()
                    .setFound(true)
                    .setFilm(filmProto)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            logger.info("Film retrieved successfully: {}", filmId);
        } catch (Exception e) {
            logger.error("Error getting film", e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void getAllFilms(GetAllFilmsRequest request, StreamObserver<GetAllFilmsResponse> responseObserver) {
        try {
            logger.info("Getting all films");

            List<FilmMetadata> allFilms = repository.findAll();
            List<Film> filmsProto = allFilms.stream()
                    .map(this::convertToFilmProto)
                    .collect(Collectors.toList());

            GetAllFilmsResponse response = GetAllFilmsResponse.newBuilder()
                    .addAllFilms(filmsProto)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            logger.info("Retrieved {} films successfully", filmsProto.size());
        } catch (Exception e) {
            logger.error("Error getting all films", e);
            responseObserver.onError(e);
        }
    }

    private Film convertToFilmProto(FilmMetadata film) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        return Film.newBuilder()
                .setFilmId(film.getId().toString())
                .setName(film.getName())
                .setDescription(film.getDescription() != null ? film.getDescription() : "")
                .setAvailable(film.isAvailable())
                .setCreatedAt(film.getCreatedAt() != null ? film.getCreatedAt().format(formatter) : "")
                .setUpdatedAt(film.getUpdatedAt() != null ? film.getUpdatedAt().format(formatter) : "")
                .build();
    }
}

