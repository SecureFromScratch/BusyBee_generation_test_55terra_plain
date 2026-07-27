package com.securefromscratch.busybee.task;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
class TaskCompletionController {

    private final TaskCompletionService taskCompletionService;

    TaskCompletionController(TaskCompletionService taskCompletionService) {
        this.taskCompletionService = taskCompletionService;
    }

    @PostMapping("/done")
    TaskCompletionResponse markDone(@RequestBody TaskCompletionRequest request) {
        return taskCompletionService.markDone(request);
    }

    @ExceptionHandler({InvalidTaskException.class, UnknownTaskException.class})
    ResponseEntity<Map<String, String>> taskCompletionError(RuntimeException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", exception.getMessage()));
    }
}
