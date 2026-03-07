package com.nabin.workflow.services.impl;

import com.nabin.workflow.exception.FileStorageException;
import com.nabin.workflow.services.interfaces.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageServiceImpl implements FileStorageService {

    private final Path fileStorageLocation;

    public FileStorageServiceImpl(@Value("${file.upload-dir:uploads}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        init();
    }

    @Override
    public void init() {
        try {
            Files.createDirectories(this.fileStorageLocation);
            log.info("✅ File storage directory created: {}", this.fileStorageLocation);
        } catch (IOException e) {
            log.error("❌ Could not create upload directory", e);
            throw new FileStorageException("Could not create upload directory", e);
        }
    }

    @Override
    public String storeFile(MultipartFile file, Long taskId) {
        // Validate filename
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            // Check for invalid characters
            if (originalFileName.contains("..")) {
                throw new FileStorageException("Invalid file path: " + originalFileName);
            }

            // Generate unique filename
            String fileExtension = "";
            int lastDotIndex = originalFileName.lastIndexOf('.');
            if (lastDotIndex > 0) {
                fileExtension = originalFileName.substring(lastDotIndex);
            }

            String fileName = UUID.randomUUID().toString() + fileExtension;

            // Create task-specific directory
            Path taskDirectory = this.fileStorageLocation.resolve("task-" + taskId);
            Files.createDirectories(taskDirectory);

            // Copy file to target location
            Path targetLocation = taskDirectory.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            log.info("✅ File stored: {} → {}", originalFileName, fileName);

            return "task-" + taskId + "/" + fileName;

        } catch (IOException e) {
            log.error("❌ Failed to store file: {}", originalFileName, e);
            throw new FileStorageException("Failed to store file: " + originalFileName, e);
        }
    }

    @Override
    public Resource loadFileAsResource(String filePath) {
        try {
            Path file = this.fileStorageLocation.resolve(filePath).normalize();
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() && resource.isReadable()) {
                log.info("✅ File loaded: {}", filePath);
                return resource;
            } else {
                log.error("❌ File not found or not readable: {}", filePath);
                throw new FileStorageException("File not found: " + filePath);
            }
        } catch (MalformedURLException e) {
            log.error("❌ Malformed file path: {}", filePath, e);
            throw new FileStorageException("File not found: " + filePath, e);
        }
    }

    @Override
    public void deleteFile(String filePath) {
        try {
            Path file = this.fileStorageLocation.resolve(filePath).normalize();
            Files.deleteIfExists(file);
            log.info("✅ File deleted: {}", filePath);
        } catch (IOException e) {
            log.error("❌ Failed to delete file: {}", filePath, e);
            throw new FileStorageException("Failed to delete file: " + filePath, e);
        }
    }
}