package com.securefromscratch.busybee.preview;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
class TaskPreviewDeletionController {

    private final TaskPreviewDeletionService taskPreviewDeletionService;

    TaskPreviewDeletionController(TaskPreviewDeletionService taskPreviewDeletionService) {
        this.taskPreviewDeletionService = taskPreviewDeletionService;
    }

    @PostMapping("/link-preview/task/delete")
    PreviewDeleteResponse deleteTaskPreview(@RequestBody TaskPreviewDeleteRequest request) {
        return taskPreviewDeletionService.deleteTaskPreview(request);
    }

    @ExceptionHandler(InvalidPreviewRequestException.class)
    ResponseEntity<Map<String, String>> invalidPreviewRequest(InvalidPreviewRequestException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", exception.getMessage()));
    }
}
