package com.securefromscratch.busybee.comment;

import java.util.List;

import com.securefromscratch.busybee.preview.CommentPreviewListingService;

import org.springframework.stereotype.Service;

@Service
public class CommentListingService {

    private final CommentRepository commentRepository;
    private final ImageMetadataRepository imageMetadataRepository;
    private final AttachmentMetadataRepository attachmentMetadataRepository;
    private final CommentPreviewListingService commentPreviewListingService;

    CommentListingService(
            CommentRepository commentRepository,
            ImageMetadataRepository imageMetadataRepository,
            AttachmentMetadataRepository attachmentMetadataRepository,
            CommentPreviewListingService commentPreviewListingService
    ) {
        this.commentRepository = commentRepository;
        this.imageMetadataRepository = imageMetadataRepository;
        this.attachmentMetadataRepository = attachmentMetadataRepository;
        this.commentPreviewListingService = commentPreviewListingService;
    }

    public List<CommentListResponse> listComments(String taskId) {
        return commentRepository.findByTaskId(taskId).stream()
                .map(comment -> commentResponse(comment))
                .toList();
    }

    private CommentListResponse commentResponse(StoredComment comment) {
        StoredImage image = comment.imageId() == null
                ? null
                : imageMetadataRepository.findImage(comment.imageId()).orElse(null);
        StoredAttachment attachment = comment.attachmentId() == null
                ? null
                : attachmentMetadataRepository.findAttachment(comment.attachmentId()).orElse(null);
        return new CommentListResponse(
                        comment.commentId(),
                        comment.text(),
                        comment.imageId(),
                        image == null ? null : image.originalFilename(),
                        comment.attachmentId(),
                        attachment == null ? null : attachment.originalFilename(),
                        comment.indent(),
                        comment.createdBy(),
                        comment.createdAt(),
                        commentPreviewListingService.listCommentPreviews(comment.commentId())
                );
    }
}
