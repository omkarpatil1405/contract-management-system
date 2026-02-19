package com.cms.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
public class FileStorageService {

    private static final String UPLOAD_DIR = "uploads";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB
    private static final List<String> ALLOWED_TYPES = List.of(
            "application/pdf",
            "image/jpeg",
            "image/png"
    );

    /**
     * Store a file on disk and return the generated unique filename.
     */
    public String storeFile(MultipartFile file) throws IOException {
        // Validate not empty
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty. Please select a file to upload.");
        }

        // Validate size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds the maximum limit of 5 MB.");
        }

        // Validate content type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Invalid file type. Only PDF, JPG, and PNG are allowed.");
        }

        // Sanitize original filename (remove path traversal chars)
        String originalName = file.getOriginalFilename();
        if (originalName == null) originalName = "file";
        originalName = Paths.get(originalName).getFileName().toString(); // strip path
        originalName = originalName.replaceAll("[^a-zA-Z0-9.\\-_]", "_"); // sanitize

        // Generate unique filename
        String uniqueName = System.currentTimeMillis() + "_" + originalName;

        // Create upload directory if needed
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Save file
        Path targetPath = uploadPath.resolve(uniqueName);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        return uniqueName;
    }

    /**
     * Load a file as a Path, with path traversal protection.
     */
    public Path loadFile(String fileName) {
        // Prevent path traversal
        String sanitized = Paths.get(fileName).getFileName().toString();
        Path filePath = Paths.get(UPLOAD_DIR).resolve(sanitized).normalize();

        if (!Files.exists(filePath)) {
            throw new RuntimeException("File not found: " + sanitized);
        }

        return filePath;
    }

    /**
     * Delete a file from uploads directory.
     */
    public void deleteFile(String fileName) {
        if (fileName == null || fileName.isBlank()) return;
        try {
            String sanitized = Paths.get(fileName).getFileName().toString();
            Path filePath = Paths.get(UPLOAD_DIR).resolve(sanitized);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // Log but don't fail — file may already be deleted
        }
    }
}
