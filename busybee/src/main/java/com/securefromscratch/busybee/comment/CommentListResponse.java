package com.securefromscratch.busybee.comment;

import java.time.LocalDateTime;
import java.util.List;

import com.securefromscratch.busybee.preview.TaskLinkPreviewResponse;

public record CommentListResponse(
        String commentid,
        String text,
        String image,
        String imageFilename,
        String attachment,
        String attachmentFilename,
        int indent,
        String createdBy,
        LocalDateTime createdOn,
        List<TaskLinkPreviewResponse> linkPreviews
) {
}
