package ru.mipt.movies.content.service;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.errors.MinioException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.UUID;

@Service
public class MinIOService {

    private static final Logger log = LoggerFactory.getLogger(MinIOService.class);
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
    }

    public InputStream getRawVideoStream(UUID filmId) {
        return getVideoStream(filmId, "raw");
    }

    public InputStream getVideoStream(UUID filmId, String quality) {
        try {
            String objectName = filmId.toString() + "_" + quality;
            log.info("Getting video stream from MinIO: bucket={}, object={}, quality={}", bucketName, objectName,
                    quality);

            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
        } catch (MinioException e) {
            log.error("MinIO error getting video for film ID: {}, quality: {}", filmId, quality, e);
            throw new RuntimeException("Failed to get video from MinIO", e);
        } catch (Exception e) {
            log.error("Error getting video from MinIO for film ID: {}, quality: {}", filmId, quality, e);
            throw new RuntimeException("Failed to get video from MinIO", e);
        }
    }

    public void uploadProcessedVideo(UUID filmId, String quality, InputStream inputStream, long size) {
        try {
            String objectName = filmId.toString() + "_" + quality;
            log.info("Uploading processed video to MinIO: bucket={}, object={}, quality={}, size={}",
                    bucketName, objectName, quality, size);

            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(inputStream, size, -1)
                    .contentType("video/mp4")
                    .build());

            log.info("Processed video uploaded successfully: filmId={}, quality={}", filmId, quality);
        } catch (MinioException e) {
            log.error("MinIO error uploading processed video for film ID: {}, quality: {}", filmId, quality, e);
            throw new RuntimeException("Failed to upload processed video to MinIO", e);
        } catch (Exception e) {
            log.error("Error uploading processed video to MinIO for film ID: {}, quality: {}", filmId, quality, e);
            throw new RuntimeException("Failed to upload processed video to MinIO", e);
        }
    }

    public long getRawVideoSize(UUID filmId) {
        return getVideoSize(filmId, "raw");
    }

    public long getVideoSize(UUID filmId, String quality) {
        try {
            String objectName = filmId.toString() + "_" + quality;
            log.debug("Getting video size from MinIO: bucket={}, object={}, quality={}", bucketName, objectName,
                    quality);

            var statObjectResponse = minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());

            return statObjectResponse.size();
        } catch (MinioException e) {
            log.error("MinIO error getting video size for film ID: {}, quality: {}", filmId, quality, e);
            throw new RuntimeException("Failed to get video size from MinIO", e);
        } catch (Exception e) {
            log.error("Error getting video size from MinIO for film ID: {}, quality: {}", filmId, quality, e);
            throw new RuntimeException("Failed to get video size from MinIO", e);
        }
    }

    public InputStream getRawVideoStreamRange(UUID filmId, long offset, long length) {
        return getVideoStreamRange(filmId, "raw", offset, length);
    }

    public InputStream getVideoStreamRange(UUID filmId, String quality, long offset, long length) {
        try {
            String objectName = filmId.toString() + "_" + quality;
            log.debug("Getting video stream range from MinIO: bucket={}, object={}, quality={}, offset={}, length={}",
                    bucketName, objectName, quality, offset, length);

            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .offset(offset)
                    .length(length)
                    .build());
        } catch (MinioException e) {
            log.error("MinIO error getting video range for film ID: {}, quality: {}", filmId, quality, e);
            throw new RuntimeException("Failed to get video range from MinIO", e);
        } catch (Exception e) {
            log.error("Error getting video range from MinIO for film ID: {}, quality: {}", filmId, quality, e);
            throw new RuntimeException("Failed to get video range from MinIO", e);
        }
    }
}
