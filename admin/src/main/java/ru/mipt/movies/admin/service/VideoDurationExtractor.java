package ru.mipt.movies.admin.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class VideoDurationExtractor {

    private static final Logger logger = LoggerFactory.getLogger(VideoDurationExtractor.class);
    private static final Pattern DURATION_PATTERN = Pattern.compile("Duration: (\\d{2}):(\\d{2}):(\\d{2})\\.(\\d{2})");

    public int extractDuration(MultipartFile file) {
        File tempFile = null;
        try {
            // Create temporary file
            tempFile = File.createTempFile("video_", ".mp4");
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(file.getBytes());
            }

            // Use ffprobe to get duration
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "ffprobe",
                    "-v", "error",
                    "-show_entries", "format=duration",
                    "-of", "default=noprint_wrappers=1:nokey=1",
                    tempFile.getAbsolutePath()
            );

            Process process = processBuilder.start();
            
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line = reader.readLine();
                if (line != null && !line.trim().isEmpty()) {
                    double durationSeconds = Double.parseDouble(line.trim());
                    int duration = (int) Math.round(durationSeconds);
                    logger.info("Extracted video duration: {} seconds", duration);
                    return duration;
                }
            }

            // Fallback: try alternative ffprobe format
            processBuilder = new ProcessBuilder(
                    "ffprobe",
                    "-v", "error",
                    "-show_format",
                    tempFile.getAbsolutePath()
            );
            
            process = processBuilder.start();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    Matcher matcher = DURATION_PATTERN.matcher(line);
                    if (matcher.find()) {
                        int hours = Integer.parseInt(matcher.group(1));
                        int minutes = Integer.parseInt(matcher.group(2));
                        int seconds = Integer.parseInt(matcher.group(3));
                        int totalSeconds = hours * 3600 + minutes * 60 + seconds;
                        logger.info("Extracted video duration: {} seconds", totalSeconds);
                        return totalSeconds;
                    }
                }
            }

            logger.warn("Could not extract video duration, using default 0");
            return 0;
        } catch (Exception e) {
            logger.error("Error extracting video duration", e);
            return 0;
        } finally {
            // Clean up temporary file
            if (tempFile != null && tempFile.exists()) {
                try {
                    Files.delete(tempFile.toPath());
                } catch (Exception e) {
                    logger.warn("Failed to delete temporary file: {}", tempFile.getAbsolutePath(), e);
                }
            }
        }
    }
}

