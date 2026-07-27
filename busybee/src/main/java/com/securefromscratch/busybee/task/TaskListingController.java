package com.securefromscratch.busybee.task;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class TaskListingController {

    private final TaskListingService taskListingService;

    TaskListingController(TaskListingService taskListingService) {
        this.taskListingService = taskListingService;
    }

    @GetMapping("/tasks")
    List<TaskListResponse> listTasks() {
        return taskListingService.listTasks();
    }
}
