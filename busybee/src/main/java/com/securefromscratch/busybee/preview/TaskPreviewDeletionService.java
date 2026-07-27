package com.securefromscratch.busybee.preview;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class TaskPreviewDeletionService {

    private final TaskPreviewRepository taskPreviewRepository;

    TaskPreviewDeletionService(TaskPreviewRepository taskPreviewRepository) {
        this.taskPreviewRepository = taskPreviewRepository;
    }

    @Transactional
    PreviewDeleteResponse deleteTaskPreview(TaskPreviewDeleteRequest request) {
        String taskId = TaskPreviewUrlSupport.validTaskId(request.taskid());
        String taskDescription = taskPreviewRepository.findTaskDescription(taskId)
                .orElseThrow(() -> new InvalidPreviewRequestException("Task was not found."));
        if (!TaskPreviewUrlSupport.taskContainsUrl(taskDescription, request.url())) {
            throw new InvalidPreviewRequestException("URL is not present in this task.");
        }

        taskPreviewRepository.deleteTaskPreview(taskId, request.url());
        return new PreviewDeleteResponse(true);
    }

}
