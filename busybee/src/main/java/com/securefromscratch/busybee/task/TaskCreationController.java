package com.securefromscratch.busybee.task;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
class TaskCreationController {

    private final TaskCreationService taskCreationService;

    TaskCreationController(TaskCreationService taskCreationService) {
        this.taskCreationService = taskCreationService;
    }

    @PostMapping("/create")
    TaskCreateResponse createTask(Authentication authentication, @RequestBody TaskCreateRequest request) {
        return taskCreationService.createTask(authentication.getName(), request);
    }

    @ExceptionHandler(InvalidTaskException.class)
    ResponseEntity<Map<String, String>> invalidTask(InvalidTaskException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", exception.getMessage()));
    }
}
