package com.securefromscratch.busybee.preview;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class TaskPreviewListingService {

    private final TaskPreviewRepository taskPreviewRepository;

    TaskPreviewListingService(TaskPreviewRepository taskPreviewRepository) {
        this.taskPreviewRepository = taskPreviewRepository;
    }

    public List<TaskLinkPreviewResponse> listTaskPreviews(String taskId) {
        return taskPreviewRepository.findTaskPreviews(taskId).stream()
                .map(preview -> new TaskLinkPreviewResponse(
                        preview.url(),
                        preview.title(),
                        preview.description(),
                        preview.image()
                ))
                .toList();
    }
}
