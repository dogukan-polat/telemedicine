package com.dogukanpolat.telemedicine.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    private final FileEncryptionService encryptionService;

    public String storeFile(MultipartFile file, UUID patientId, boolean encrypt) throws Exception {
        String originalFileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String secureFileName = encryptionService.generateSecureFileName(originalFileName);

        // Create patient-specific directory
        Path patientDir = Paths.get(uploadDir, patientId.toString());
        Files.createDirectories(patientDir);

        Path targetLocation = patientDir.resolve(secureFileName);

        if (encrypt) {
            // Encrypt file before storing
            byte[] fileData = file.getBytes();
            byte[] encryptedData = encryptionService.encrypt(fileData);
            Files.write(targetLocation, encryptedData);
            log.info("Encrypted file stored: {}", secureFileName);
        } else {
            // Store file as-is
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }
            log.info("File stored: {}", secureFileName);
        }

        return patientId + "/" + secureFileName;
    }

    public Resource loadFileAsResource(String filePath, boolean decrypt) throws Exception {
        Path file = Paths.get(uploadDir).resolve(filePath).normalize();
        Resource resource = new UrlResource(file.toUri());

        if (!resource.exists()) {
            throw new IOException("File not found: " + filePath);
        }

        if (decrypt) {
            // Read encrypted file and decrypt
            byte[] encryptedData = Files.readAllBytes(file);
            byte[] decryptedData = encryptionService.decrypt(encryptedData);

            // Create temporary file with decrypted data
            Path tempFile = Files.createTempFile("decrypted_", ".tmp");
            Files.write(tempFile, decryptedData);
            resource = new UrlResource(tempFile.toUri());

            log.debug("File decrypted: {}", filePath);
        }

        return resource;
    }

    public void deleteFile(String filePath) throws IOException {
        Path file = Paths.get(uploadDir).resolve(filePath).normalize();
        Files.deleteIfExists(file);
        log.info("File deleted: {}", filePath);
    }

    public long getFileSize(String filePath) throws IOException {
        Path file = Paths.get(uploadDir).resolve(filePath).normalize();
        return Files.size(file);
    }

    public boolean fileExists(String filePath) {
        Path file = Paths.get(uploadDir).resolve(filePath).normalize();
        return Files.exists(file);
    }

    public void initializeStorage() {
        try {
            Path uploadPath = Paths.get(uploadDir);
            Files.createDirectories(uploadPath);
            log.info("File storage initialized at: {}", uploadPath.toAbsolutePath());
        } catch (IOException e) {
            log.error("Could not initialize file storage", e);
            throw new RuntimeException("Could not initialize file storage", e);
        }
    }
}