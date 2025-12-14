package ru.mipt.movies.meta.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.mipt.movies.meta.model.FilmMetadata;

import java.util.UUID;

@Repository
public interface FilmMetadataRepository extends JpaRepository<FilmMetadata, UUID> {
}
