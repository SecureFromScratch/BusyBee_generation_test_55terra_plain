package com.securefromscratch.busybee.task;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

record TaskToCreate(
        String taskId,
        String name,
        String description,
        LocalDate dueDate,
        LocalTime dueTime,
        String createdBy,
        List<String> responsibilityOf
) {
}
