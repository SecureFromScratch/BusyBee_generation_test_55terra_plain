package com.securefromscratch.busybee.preview;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class CommentPreviewGenerationService {

    private final CommentPreviewRepository commentPreviewRepository;
    private final TaskPreviewMetadataFetcher taskPreviewMetadataFetcher;

    CommentPreviewGenerationService(
            CommentPreviewRepository commentPreviewRepository,
            TaskPreviewMetadataFetcher taskPreviewMetadataFetcher
    ) {
        this.commentPreviewRepository = commentPreviewRepository;
        this.taskPreviewMetadataFetcher = taskPreviewMetadataFetcher;
    }

    @Transactional
    TaskLinkPreviewResponse generate(CommentPreviewRequest request, String username) {
        String commentId = TaskPreviewUrlSupport.validCommentId(request.commentid());
        String commentText = commentPreviewRepository.findCommentText(commentId)
                .orElseThrow(() -> new InvalidPreviewRequestException("Comment was not found."));
        if (!TaskPreviewUrlSupport.textContainsUrl(commentText, request.url())) {
            throw new InvalidPreviewRequestException("URL is not present in this comment.");
        }

        GeneratedTaskPreview generated = taskPreviewMetadataFetcher.fetch(request.url());
        commentPreviewRepository.replaceCommentPreview(commentId, generated, username);
        return new TaskLinkPreviewResponse(
                generated.url(),
                generated.title(),
                generated.description(),
                generated.image()
        );
    }
}
