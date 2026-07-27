package com.securefromscratch.busybee.task;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class TaskCompletionService {

    private final TaskCompletionRepository taskCompletionRepository;

    TaskCompletionService(TaskCompletionRepository taskCompletionRepository) {
        this.taskCompletionRepository = taskCompletionRepository;
    }

    @Transactional
    TaskCompletionResponse markDone(TaskCompletionRequest request) {
        String taskId = validTaskId(request.taskid());
        if (!taskCompletionRepository.markDone(taskId)) {
            throw new UnknownTaskException();
        }

        return new TaskCompletionResponse(true);
    }

    private String validTaskId(String taskId) {
        if (taskId == null) {
            throw new InvalidTaskException("Task ID is invalid.");
        }

        try {
            return UUID.fromString(taskId).toString();
        } catch (IllegalArgumentException exception) {
            throw new InvalidTaskException("Task ID is invalid.");
        }
    }
}
