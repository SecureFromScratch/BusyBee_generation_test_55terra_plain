package com.securefromscratch.busybee.task;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.securefromscratch.busybee.comment.CommentListResponse;
import com.securefromscratch.busybee.ai.CommentSummaryResponse;
import com.securefromscratch.busybee.preview.TaskLinkPreviewResponse;

record TaskListResponse(
        String taskid,
        String name,
        String desc,
        LocalDate dueDate,
        String dueTime,
        String createdBy,
        List<String> responsibilityOf,
        LocalDateTime creationDatetime,
        boolean done,
        CommentSummaryResponse commentSummary,
        List<TaskLinkPreviewResponse> linkPreviews,
        List<CommentListResponse> comments
) {
}
