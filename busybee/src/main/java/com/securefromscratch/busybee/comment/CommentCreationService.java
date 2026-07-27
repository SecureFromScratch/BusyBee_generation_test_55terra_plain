package com.securefromscratch.busybee.comment;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class CommentCreationService {

    private final CommentRepository commentRepository;
    private final ImageStorageService imageStorageService;
    private final ImageMetadataRepository imageMetadataRepository;
    private final AttachmentStorageService attachmentStorageService;
    private final AttachmentMetadataRepository attachmentMetadataRepository;

    CommentCreationService(
            CommentRepository commentRepository,
            ImageStorageService imageStorageService,
            ImageMetadataRepository imageMetadataRepository,
            AttachmentStorageService attachmentStorageService,
            AttachmentMetadataRepository attachmentMetadataRepository
    ) {
        this.commentRepository = commentRepository;
        this.imageStorageService = imageStorageService;
        this.imageMetadataRepository = imageMetadataRepository;
        this.attachmentStorageService = attachmentStorageService;
        this.attachmentMetadataRepository = attachmentMetadataRepository;
    }

    @Transactional
    CommentCreateResponse createComment(String username, CommentCreateRequest request, ImageUpload imageUpload) {
        String taskId = validTaskId(request.taskid());
        if (request.text() == null || request.text().isBlank()) {
            throw new InvalidCommentException("Comment text is required.");
        }
        if (!commentRepository.taskExists(taskId)) {
            throw new InvalidCommentException("Task was not found.");
        }

        CommentParent parent = parentComment(request.commentid(), taskId);

        String commentId = UUID.randomUUID().toString();
        StoredImage image = imageUpload == null || !isImageUpload(imageUpload)
                ? null
                : imageStorageService.store(imageUpload);
        StoredAttachment attachment = imageUpload == null || image != null
                ? null
                : attachmentStorageService.store(new AttachmentUpload(imageUpload.originalFilename(), imageUpload.content()));
        if (image != null) {
            imageMetadataRepository.save(image, username);
        }
        if (attachment != null) {
            attachmentMetadataRepository.save(attachment, username);
        }
        commentRepository.create(
                commentId,
                taskId,
                parent == null ? null : parent.commentId(),
                request.text(),
                image == null ? null : image.fileId(),
                attachment == null ? null : attachment.fileId(),
                parent == null ? 0 : parent.indent() + 1,
                username
        );
        return new CommentCreateResponse(commentId);
    }

    private String validTaskId(String taskId) {
        if (taskId == null) {
            throw new InvalidCommentException("Task ID is invalid.");
        }

        try {
            return UUID.fromString(taskId).toString();
        } catch (IllegalArgumentException exception) {
            throw new InvalidCommentException("Task ID is invalid.");
        }
    }

    private CommentParent parentComment(String parentCommentId, String taskId) {
        if (parentCommentId == null) {
            return null;
        }

        String validParentCommentId = validCommentId(parentCommentId);
        CommentParent parent = commentRepository.findParent(validParentCommentId)
                .orElseThrow(() -> new InvalidCommentException("Parent comment was not found."));
        if (!taskId.equals(parent.taskId())) {
            throw new InvalidCommentException("Parent comment belongs to another task.");
        }
        return parent;
    }

    private String validCommentId(String commentId) {
        try {
            return UUID.fromString(commentId).toString();
        } catch (IllegalArgumentException exception) {
            throw new InvalidCommentException("Parent comment ID is invalid.");
        }
    }

    private boolean isImageUpload(ImageUpload upload) {
        String filename = upload.originalFilename();
        if (filename == null) {
            return false;
        }

        String lowercaseFilename = filename.toLowerCase(java.util.Locale.ROOT);
        return lowercaseFilename.endsWith(".png")
                || lowercaseFilename.endsWith(".jpg")
                || lowercaseFilename.endsWith(".jpeg")
                || lowercaseFilename.endsWith(".gif")
                || lowercaseFilename.endsWith(".webp")
                || lowercaseFilename.endsWith(".svg");
    }
}
