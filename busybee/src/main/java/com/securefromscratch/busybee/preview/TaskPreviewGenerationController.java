package com.securefromscratch.busybee.preview;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
class TaskPreviewGenerationController {

    private final TaskPreviewGenerationService taskPreviewGenerationService;

    TaskPreviewGenerationController(TaskPreviewGenerationService taskPreviewGenerationService) {
        this.taskPreviewGenerationService = taskPreviewGenerationService;
    }

    @PostMapping("/link-preview/task")
    TaskLinkPreviewResponse generate(
            Authentication authentication,
            @RequestBody TaskPreviewGenerateRequest request
    ) {
        return taskPreviewGenerationService.generate(request, authentication.getName());
    }

    @ExceptionHandler(InvalidPreviewRequestException.class)
    ResponseEntity<Map<String, String>> invalidPreviewRequest(InvalidPreviewRequestException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", exception.getMessage()));
    }
}
