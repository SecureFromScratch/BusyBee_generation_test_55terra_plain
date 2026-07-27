package com.securefromscratch.busybee.task;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

record TaskCreateRequest(
        String name,
        String desc,
        LocalDate dueDate,
        LocalTime dueTime,
        List<String> responsibilityOf
) {
}
