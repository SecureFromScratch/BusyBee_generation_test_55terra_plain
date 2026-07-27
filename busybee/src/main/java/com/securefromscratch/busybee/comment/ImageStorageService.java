package com.securefromscratch.busybee.comment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ImageStorageService {

    private static final Map<String, String> IMAGE_CONTENT_TYPES = Map.of(
            "png", "image/png",
            "jpg", "image/jpeg",
            "jpeg", "image/jpeg",
            "gif", "image/gif",
            "webp", "image/webp",
            "svg", "image/svg+xml"
    );

    private final Path storageDirectory;

    ImageStorageService(@Value("${busybee.file-storage.directory}") String storageDirectory) {
        this.storageDirectory = Path.of(storageDirectory).toAbsolutePath().normalize();
    }

    StoredImage store(ImageUpload upload) {
        String originalFilename = validFilename(upload.originalFilename());
        String extension = extensionOf(originalFilename);
        String contentType = IMAGE_CONTENT_TYPES.get(extension);
        if (contentType == null) {
            throw new InvalidCommentException("Unsupported image file type.");
        }
        if (upload.content().length == 0) {
            throw new InvalidCommentException("Image file is empty.");
        }

        String fileId = UUID.randomUUID().toString();
        String storageName = fileId + ".bin";
        Path storagePath = storageDirectory.resolve(storageName).normalize();
        if (!storagePath.startsWith(storageDirectory)) {
            throw new InvalidCommentException("Image filename is invalid.");
        }

        try {
            Files.createDirectories(storageDirectory);
            Files.write(storagePath, upload.content(), StandardOpenOption.CREATE_NEW);
        } catch (IOException exception) {
            throw new ImageStorageException("Image could not be stored.", exception);
        }

        return new StoredImage(fileId, originalFilename, contentType, storageName);
    }

    byte[] load(StoredImage image) {
        Path storagePath = pathFor(image);

        try {
            return Files.readAllBytes(storagePath);
        } catch (IOException exception) {
            throw new ImageStorageException("Image could not be loaded.", exception);
        }
    }

    public Path pathFor(StoredImage image) {
        Path storagePath = storageDirectory.resolve(image.storageName()).normalize();
        if (!storagePath.startsWith(storageDirectory)) {
            throw new ImageStorageException("Image could not be loaded.");
        }
        return storagePath;
    }

    private String validFilename(String filename) {
        if (filename == null || filename.isBlank() || filename.contains("/") || filename.contains("\\")) {
            throw new InvalidCommentException("Image filename is invalid.");
        }

        return filename;
    }

    private String extensionOf(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot <= 0 || lastDot == filename.length() - 1) {
            throw new InvalidCommentException("Unsupported image file type.");
        }

        return filename.substring(lastDot + 1).toLowerCase(Locale.ROOT);
    }
}
