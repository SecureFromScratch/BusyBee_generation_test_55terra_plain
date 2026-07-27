package com.securefromscratch.busybee.task;

import java.util.List;

import com.securefromscratch.busybee.comment.CommentListingService;
import com.securefromscratch.busybee.ai.CommentSummaryListingService;
import com.securefromscratch.busybee.preview.TaskPreviewListingService;

import org.springframework.stereotype.Service;

@Service
class TaskListingService {

    private final TaskListingRepository taskListingRepository;
    private final CommentListingService commentListingService;
    private final TaskPreviewListingService taskPreviewListingService;
    private final CommentSummaryListingService commentSummaryListingService;

    TaskListingService(
            TaskListingRepository taskListingRepository,
            CommentListingService commentListingService,
            TaskPreviewListingService taskPreviewListingService,
            CommentSummaryListingService commentSummaryListingService
    ) {
        this.taskListingRepository = taskListingRepository;
        this.commentListingService = commentListingService;
        this.taskPreviewListingService = taskPreviewListingService;
        this.commentSummaryListingService = commentSummaryListingService;
    }

    List<TaskListResponse> listTasks() {
        return taskListingRepository.findAll().stream()
                .map(task -> new TaskListResponse(
                        task.taskId(),
                        task.name(),
                        task.description(),
                        task.dueDate(),
                        task.dueTime() == null ? null : task.dueTime().toString(),
                        task.createdBy(),
                        taskListingRepository.findResponsibilities(task.taskId()),
                        task.createdAt(),
                        task.done(),
                        commentSummaryListingService.summaryFor(task.taskId()),
                        taskPreviewListingService.listTaskPreviews(task.taskId()),
                        commentListingService.listComments(task.taskId())
                ))
                .toList();
    }
}
