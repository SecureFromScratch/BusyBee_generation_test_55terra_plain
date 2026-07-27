package com.securefromscratch.busybee.preview;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class CommentPreviewDeletionService {

    private final CommentPreviewRepository commentPreviewRepository;

    CommentPreviewDeletionService(CommentPreviewRepository commentPreviewRepository) {
        this.commentPreviewRepository = commentPreviewRepository;
    }

    @Transactional
    PreviewDeleteResponse delete(CommentPreviewRequest request) {
        String commentId = TaskPreviewUrlSupport.validCommentId(request.commentid());
        String commentText = commentPreviewRepository.findCommentText(commentId)
                .orElseThrow(() -> new InvalidPreviewRequestException("Comment was not found."));
        if (!TaskPreviewUrlSupport.textContainsUrl(commentText, request.url())) {
            throw new InvalidPreviewRequestException("URL is not present in this comment.");
        }

        commentPreviewRepository.deleteCommentPreview(commentId, request.url());
        return new PreviewDeleteResponse(true);
    }
}
