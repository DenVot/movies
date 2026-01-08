package ru.mipt.movies.admin.service;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.PutObjectArgs;
import io.minio.errors.MinioException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class MinIOService {

    private static final Logger logger = LoggerFactory.getLogger(MinIOService.class);

    private final MinioClient minioClient;
    private final String bucketName;

    public MinIOService(
            @Value("${minio.endpoint}") String endpoint,
            @Value("${minio.access-key}") String accessKey,
            @Value("${minio.secret-key}") String secretKey,
            @Value("${minio.bucket-name}") String bucketName) {
        this.bucketName = bucketName;
        this.minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
        initializeBucket();
    }

    private void initializeBucket() {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build());
                logger.info("Created bucket: {}", bucketName);
            } else {
                logger.info("Bucket already exists: {}", bucketName);
            }
        } catch (Exception e) {
            logger.error("Error initializing bucket: {}", bucketName, e);
            throw new RuntimeException("Failed to initialize MinIO bucket", e);
        }
    }

    public String uploadVideo(UUID filmId, InputStream inputStream, long size, String contentType) {
        try {
            String objectName = filmId.toString() + "_raw";
            
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(inputStream, size, -1)
                    .contentType(contentType)
                    .build());

            logger.info("Video uploaded to MinIO: bucket={}, object={}, size={}", 
                    bucketName, objectName, size);
            
            return objectName;
        } catch (MinioException e) {
            logger.error("MinIO error uploading video for film ID: {}", filmId, e);
            throw new RuntimeException("Failed to upload video to MinIO", e);
        } catch (Exception e) {
            logger.error("Error uploading video to MinIO for film ID: {}", filmId, e);
            throw new RuntimeException("Failed to upload video to MinIO", e);
        }
    }

    public String replaceVideo(UUID filmId, InputStream inputStream, long size, String contentType) {
        try {
            deleteVideo(filmId);
            return uploadVideo(filmId, inputStream, size, contentType);
        } catch (Exception e) {
            logger.error("Error replacing video for film ID: {}", filmId, e);
            throw new RuntimeException("Failed to replace video", e);
        }
    }

    public void deleteVideo(UUID filmId) {
        try {
            List<String> objectsToDelete = new ArrayList<>();
            objectsToDelete.add(filmId.toString() + "_raw");

            String[] qualities = {"360p", "480p", "720p", "1080p"};
            for (String quality : qualities) {
                objectsToDelete.add(filmId + "_" + quality);
            }

            for (String objectName : objectsToDelete) {
                try {
                    minioClient.removeObject(RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build());
                    logger.info("Deleted object from MinIO: bucket={}, object={}", bucketName, objectName);
                } catch (Exception e) {
                    logger.debug("Could not delete object (might not exist): bucket={}, object={}", bucketName, objectName);
                }
            }
            
            logger.info("Video deletion completed for film ID: {}", filmId);
        } catch (Exception e) {
            logger.error("Error deleting video from MinIO for film ID: {}", filmId, e);
            throw new RuntimeException("Failed to delete video from MinIO", e);
        }
    }
}
