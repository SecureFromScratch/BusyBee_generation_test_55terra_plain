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
class AttachmentStorageService {

    private static final Map<String, String> ATTACHMENT_CONTENT_TYPES = Map.ofEntries(
            Map.entry("txt", "text/plain"),
            Map.entry("csv", "text/csv"),
            Map.entry("pdf", "application/pdf"),
            Map.entry("doc", "application/msword"),
            Map.entry("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
            Map.entry("xls", "application/vnd.ms-excel"),
            Map.entry("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
            Map.entry("json", "application/json"),
            Map.entry("zip", "application/zip")
    );

    private final Path storageDirectory;

    AttachmentStorageService(@Value("${busybee.file-storage.directory}") String storageDirectory) {
        this.storageDirectory = Path.of(storageDirectory).toAbsolutePath().normalize();
    }

    StoredAttachment store(AttachmentUpload upload) {
        String originalFilename = validFilename(upload.originalFilename());
        String contentType = ATTACHMENT_CONTENT_TYPES.get(extensionOf(originalFilename));
        if (contentType == null) {
            throw new InvalidCommentException("Unsupported attachment file type.");
        }
        if (upload.content().length == 0) {
            throw new InvalidCommentException("Attachment file is empty.");
        }

        String fileId = UUID.randomUUID().toString();
        String storageName = fileId + ".bin";
        Path storagePath = storageDirectory.resolve(storageName).normalize();
        if (!storagePath.startsWith(storageDirectory)) {
            throw new InvalidCommentException("Attachment filename is invalid.");
        }

        try {
            Files.createDirectories(storageDirectory);
            Files.write(storagePath, upload.content(), StandardOpenOption.CREATE_NEW);
        } catch (IOException exception) {
            throw new AttachmentStorageException("Attachment could not be stored.", exception);
        }

        return new StoredAttachment(fileId, originalFilename, contentType, storageName);
    }

    byte[] load(StoredAttachment attachment) {
        Path storagePath = storageDirectory.resolve(attachment.storageName()).normalize();
        if (!storagePath.startsWith(storageDirectory)) {
            throw new AttachmentStorageException("Attachment could not be loaded.");
        }

        try {
            return Files.readAllBytes(storagePath);
        } catch (IOException exception) {
            throw new AttachmentStorageException("Attachment could not be loaded.", exception);
        }
    }

    private String validFilename(String filename) {
        if (filename == null || filename.isBlank() || filename.contains("/") || filename.contains("\\")) {
            throw new InvalidCommentException("Attachment filename is invalid.");
        }

        return filename;
    }

    private String extensionOf(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot <= 0 || lastDot == filename.length() - 1) {
            throw new InvalidCommentException("Unsupported attachment file type.");
        }

        return filename.substring(lastDot + 1).toLowerCase(Locale.ROOT);
    }
}
