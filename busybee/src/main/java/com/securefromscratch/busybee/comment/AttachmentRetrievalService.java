package com.securefromscratch.busybee.comment;

import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
class AttachmentRetrievalService {

    private final AttachmentMetadataRepository attachmentMetadataRepository;
    private final AttachmentStorageService attachmentStorageService;

    AttachmentRetrievalService(
            AttachmentMetadataRepository attachmentMetadataRepository,
            AttachmentStorageService attachmentStorageService
    ) {
        this.attachmentMetadataRepository = attachmentMetadataRepository;
        this.attachmentStorageService = attachmentStorageService;
    }

    RetrievedAttachment findAttachment(String attachmentId) {
        String validAttachmentId = validAttachmentId(attachmentId);
        StoredAttachment attachment = attachmentMetadataRepository.findAttachment(validAttachmentId)
                .orElseThrow(() -> new AttachmentStorageException("Attachment was not found."));
        return new RetrievedAttachment(
                attachment.originalFilename(),
                attachment.contentType(),
                attachmentStorageService.load(attachment)
        );
    }

    private String validAttachmentId(String attachmentId) {
        if (attachmentId == null) {
            throw new AttachmentStorageException("Attachment was not found.");
        }

        try {
            return UUID.fromString(attachmentId).toString();
        } catch (IllegalArgumentException exception) {
            throw new AttachmentStorageException("Attachment was not found.");
        }
    }
}
