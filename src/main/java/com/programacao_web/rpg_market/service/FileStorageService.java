package com.programacao_web.rpg_market.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class FileStorageService {
    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);
    private final Path fileStorageLocation;

    public FileStorageService(@Value("${rpg.market.file.upload-dir}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            log.info("Creating directory at: {}", this.fileStorageLocation.toString());
            Files.createDirectories(this.fileStorageLocation);
            log.info("Upload directory created successfully");
        } catch (Exception ex) {
            log.error("Could not create upload directory: {}", ex.getMessage());
            throw new RuntimeException("Não foi possível criar o diretório de upload: " + this.fileStorageLocation, ex);
        }
    }

    public String storeFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            log.warn("Attempted to store empty or null file");
            return null; // Return null for empty files rather than throwing exception
        }
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        
        String filename = UUID.randomUUID().toString() + fileExtension;
        Path targetLocation = this.fileStorageLocation.resolve(filename);
        
        try {
            // Ensure parent directories exist
            Files.createDirectories(targetLocation.getParent());
            
            // Copy file to the target location
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            log.info("File stored successfully at: {}", targetLocation);
            
            return filename;
        } catch (IOException ex) {
            log.error("Could not store file: {}", ex.getMessage(), ex);
            throw new IOException("Não foi possível armazenar o arquivo. Por favor, tente novamente!", ex);
        }
    }
    
    /**
     * Returns the Path for a specific file
     */
    public Path getFilePath(String fileName) {
        return this.fileStorageLocation.resolve(fileName);
    }
    
    /**
     * Delete a file from storage
     */
    public boolean deleteFile(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("Could not delete file: {}", e.getMessage());
            return false;
        }
    }
}