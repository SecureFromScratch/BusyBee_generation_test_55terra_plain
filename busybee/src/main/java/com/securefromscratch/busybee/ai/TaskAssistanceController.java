package com.securefromscratch.busybee.ai;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
class TaskAssistanceController {

    private final TaskAssistanceService taskAssistanceService;

    TaskAssistanceController(TaskAssistanceService taskAssistanceService) {
        this.taskAssistanceService = taskAssistanceService;
    }

    @PostMapping("/ai/task/improve")
    ImprovedTaskResponse improve(Authentication authentication, @RequestBody TaskAssistanceRequest request) {
        return taskAssistanceService.improve(authentication, request);
    }

    @PostMapping("/ai/task/subtasks")
    SubtaskSuggestionsResponse subtasks(Authentication authentication, @RequestBody TaskAssistanceRequest request) {
        return taskAssistanceService.subtasks(authentication, request);
    }

    @PostMapping("/ai/task/ocr-structure")
    OcrStructureResponse structureOcr(Authentication authentication, @RequestBody OcrStructureRequest request) {
        return taskAssistanceService.structureOcr(authentication, request);
    }

    @ExceptionHandler(InvalidTaskAssistanceException.class)
    ResponseEntity<Map<String, String>> invalidAssistance(InvalidTaskAssistanceException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", exception.getMessage()));
    }
}
