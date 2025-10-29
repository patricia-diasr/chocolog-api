package com.chocolog.api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path rootLocation;

    public FileStorageService(@Value("${chocolog.storage.file-system-path}") String path) {
        this.rootLocation = Paths.get(path);
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage directory", e);
        }
    }

    public String save(byte[] fileData, Long batchId) {
        try {
            String filename = "batch-" + batchId + "-" + UUID.randomUUID().toString() + ".pdf";
            Path destinationFile = this.rootLocation.resolve(filename);

            Files.write(destinationFile, fileData);

            return destinationFile.toAbsolutePath().toString();

        } catch (IOException e) {
            throw new RuntimeException("Could not save file.", e);
        }
    }

    public Resource loadAsResource(String fullPath) {
        try {
            Path filePath = Paths.get(fullPath);
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read file: " + fullPath);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("File path error (Malformed URL): " + fullPath, e);
        }
    }

    public String getFilename(String fullPath) {
        if (fullPath == null) {
            return null;
        }
        return Paths.get(fullPath).getFileName().toString();
    }
}