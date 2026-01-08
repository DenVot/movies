package ru.mipt.movies.content.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class VideoProcessingService {

    private static final Logger log = LoggerFactory.getLogger(VideoProcessingService.class);

    private final String ffmpegPath;
    private final List<String> qualities;
    private final MinIOService minIOService;

    public VideoProcessingService(
            @Value("${video.ffmpeg.path:}") String ffmpegPath,
            @Value("${video.qualities:360p}") String qualitiesString,
            MinIOService minIOService) {
        this.ffmpegPath = ffmpegPath.isEmpty() ? "ffmpeg" : ffmpegPath;
        this.qualities = Arrays.stream(qualitiesString.split(","))
                .map(String::trim)
                .collect(Collectors.toList());
        this.minIOService = minIOService;
        log.info("Video processing service initialized with qualities: {}", this.qualities);
    }

    public void processVideo(UUID filmId) {
        try {
            log.info("Starting video processing for film ID: {}", filmId);

            Path tempDir = Files.createTempDirectory("video-processing-" + filmId);
            Path rawVideoPath = tempDir.resolve(filmId + "_raw.mp4");

            try {
                log.info("Downloading raw video for film ID: {}", filmId);
                try (InputStream rawStream = minIOService.getRawVideoStream(filmId);
                        FileOutputStream fos = new FileOutputStream(rawVideoPath.toFile())) {
                    rawStream.transferTo(fos);
                }

                List<CompletableFuture<Void>> futures = new ArrayList<>();

                for (String quality : qualities) {
                    CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                        try {
                            processQuality(filmId, rawVideoPath, quality, tempDir);
                        } catch (Exception e) {
                            log.error("Error processing quality {} for film ID: {}", quality, filmId, e);
                            throw new RuntimeException("Failed to process quality " + quality, e);
                        }
                    });
                    futures.add(future);
                }

                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

                log.info("Successfully processed all qualities for film ID: {}", filmId);

            } finally {
                deleteDirectory(tempDir.toFile());
            }

        } catch (Exception e) {
            log.error("Error processing video for film ID: {}", filmId, e);
            throw new RuntimeException("Failed to process video", e);
        }
    }

    private void processQuality(UUID filmId, Path rawVideoPath, String quality, Path tempDir) throws Exception {
        log.info("Processing quality {} for film ID: {}", quality, filmId);

        String[] resolution = getResolution(quality);
        String width = resolution[0];
        String height = resolution[1];

        Path outputPath = tempDir.resolve(filmId + "_" + quality + ".mp4");

        List<String> command = new ArrayList<>();
        command.add(ffmpegPath);
        command.add("-i");
        command.add(rawVideoPath.toString());
        command.add("-vf");
        command.add("scale=" + width + ":" + height + ":force_original_aspect_ratio=decrease,pad=" + width + ":"
                + height + ":(ow-iw)/2:(oh-ih)/2");
        command.add("-c:v");
        command.add("libx264");
        command.add("-preset");
        command.add("medium");
        command.add("-crf");
        command.add("23");
        command.add("-c:a");
        command.add("aac");
        command.add("-b:a");
        command.add("128k");
        command.add("-movflags");
        command.add("+faststart");
        command.add("-y");
        command.add(outputPath.toString());

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("FFmpeg failed with exit code: " + exitCode);
        }

        log.info("Uploading processed video quality {} for film ID: {}", quality, filmId);
        try (FileInputStream fis = new FileInputStream(outputPath.toFile())) {
            long fileSize = Files.size(outputPath);
            minIOService.uploadProcessedVideo(filmId, quality, fis, fileSize);
        }

        log.info("Successfully processed and uploaded quality {} for film ID: {}", quality, filmId);
    }

    private String[] getResolution(String quality) {
        return switch (quality.toLowerCase()) {
            case "360p" -> new String[] { "640", "360" };
            case "480p" -> new String[] { "854", "480" };
            case "720p" -> new String[] { "1280", "720" };
            case "1080p" -> new String[] { "1920", "1080" };
            default -> throw new IllegalArgumentException("Unsupported quality: " + quality);
        };
    }

    private void deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
            directory.delete();
        }
    }

    public List<String> getAvailableQualities() {
        return new ArrayList<>(qualities);
    }
}
