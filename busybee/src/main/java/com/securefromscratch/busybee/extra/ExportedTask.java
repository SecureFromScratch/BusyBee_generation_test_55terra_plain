package com.securefromscratch.busybee.extra;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

record ExportedTask(
        String name,
        String desc,
        LocalDate dueDate,
        LocalTime dueTime,
        List<String> responsibilityOf
) {
}
