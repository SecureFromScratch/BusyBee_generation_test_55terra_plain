package com.securefromscratch.busybee.task;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

record ListedTask(
        String taskId,
        String name,
        String description,
        LocalDate dueDate,
        LocalTime dueTime,
        String createdBy,
        LocalDateTime createdAt,
        boolean done
) {
}
