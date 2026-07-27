package com.securefromscratch.busybee.comment;

import java.time.LocalDateTime;

record StoredComment(
        String commentId,
        String text,
        String imageId,
        String attachmentId,
        int indent,
        String createdBy,
        LocalDateTime createdAt
) {
}
