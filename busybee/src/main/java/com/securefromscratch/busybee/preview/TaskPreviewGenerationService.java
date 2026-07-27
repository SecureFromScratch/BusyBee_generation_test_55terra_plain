package com.securefromscratch.busybee.preview;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class TaskPreviewGenerationService {

    private final TaskPreviewRepository taskPreviewRepository;
    private final TaskPreviewMetadataFetcher taskPreviewMetadataFetcher;

    TaskPreviewGenerationService(
            TaskPreviewRepository taskPreviewRepository,
            TaskPreviewMetadataFetcher taskPreviewMetadataFetcher
    ) {
        this.taskPreviewRepository = taskPreviewRepository;
        this.taskPreviewMetadataFetcher = taskPreviewMetadataFetcher;
    }

    @Transactional
    TaskLinkPreviewResponse generate(TaskPreviewGenerateRequest request, String username) {
        String taskId = TaskPreviewUrlSupport.validTaskId(request.taskid());
        String taskDescription = taskPreviewRepository.findTaskDescription(taskId)
                .orElseThrow(() -> new InvalidPreviewRequestException("Task was not found."));
        if (!TaskPreviewUrlSupport.taskContainsUrl(taskDescription, request.url())) {
            throw new InvalidPreviewRequestException("URL is not present in this task.");
        }

        GeneratedTaskPreview generated = taskPreviewMetadataFetcher.fetch(request.url());
        taskPreviewRepository.replaceTaskPreview(taskId, generated, username);
        return new TaskLinkPreviewResponse(
                generated.url(),
                generated.title(),
                generated.description(),
                generated.image()
        );
    }
}
