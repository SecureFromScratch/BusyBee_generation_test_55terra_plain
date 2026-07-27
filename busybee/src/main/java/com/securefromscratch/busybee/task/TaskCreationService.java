package com.securefromscratch.busybee.task;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class TaskCreationService {

    private final TaskRepository taskRepository;

    TaskCreationService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Transactional
    TaskCreateResponse createTask(String username, TaskCreateRequest request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new InvalidTaskException("Task name is required.");
        }

        LocalDate dueDate = request.dueDate();
        LocalTime dueTime = dueDate == null ? null : request.dueTime();
        List<String> responsibilityOf = request.responsibilityOf() == null
                ? List.of()
                : request.responsibilityOf().stream().map(String::trim).filter(value -> !value.isEmpty()).toList();
        String taskId = UUID.randomUUID().toString();
        TaskToCreate task = new TaskToCreate(
                taskId,
                request.name().trim(),
                request.desc() == null ? "" : request.desc(),
                dueDate,
                dueTime,
                username,
                responsibilityOf
        );

        taskRepository.create(task);
        return new TaskCreateResponse(taskId);
    }
}
