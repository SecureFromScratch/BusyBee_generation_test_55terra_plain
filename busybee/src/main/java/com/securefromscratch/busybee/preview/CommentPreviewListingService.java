package com.securefromscratch.busybee.preview;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class CommentPreviewListingService {

    private final CommentPreviewRepository commentPreviewRepository;

    CommentPreviewListingService(CommentPreviewRepository commentPreviewRepository) {
        this.commentPreviewRepository = commentPreviewRepository;
    }

    public List<TaskLinkPreviewResponse> listCommentPreviews(String commentId) {
        return commentPreviewRepository.findCommentPreviews(commentId).stream()
                .map(preview -> new TaskLinkPreviewResponse(
                        preview.url(),
                        preview.title(),
                        preview.description(),
                        preview.image()
                ))
                .toList();
    }
}
