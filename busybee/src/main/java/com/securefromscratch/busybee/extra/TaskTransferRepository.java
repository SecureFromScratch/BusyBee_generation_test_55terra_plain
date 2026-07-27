package com.securefromscratch.busybee.extra;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

@Repository
class TaskTransferRepository {

    private static final org.jooq.Table<?> TASKS = DSL.table("tasks");
    private static final org.jooq.Field<String> TASK_ID = DSL.field("task_id", String.class);
    private static final org.jooq.Field<String> NAME = DSL.field("name", String.class);
    private static final org.jooq.Field<String> DESCRIPTION = DSL.field("description", String.class);
    private static final org.jooq.Field<LocalDate> DUE_DATE = DSL.field("due_date", LocalDate.class);
    private static final org.jooq.Field<LocalTime> DUE_TIME = DSL.field("due_time", LocalTime.class);
    private static final org.jooq.Field<String> CREATED_BY = DSL.field("created_by", String.class);
    private static final org.jooq.Table<?> TASK_RESPONSIBILITIES = DSL.table("task_responsibilities");
    private static final org.jooq.Field<String> RESPONSIBILITY_TASK_ID = DSL.field("task_id", String.class);
    private static final org.jooq.Field<String> RESPONSIBLE_NAME = DSL.field("responsible_name", String.class);
    private static final org.jooq.Field<Integer> POSITION_INDEX = DSL.field("position_index", Integer.class);

    private final DSLContext dsl;

    TaskTransferRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    List<ExportedTask> findExportedTasks() {
        return dsl.select(TASK_ID, NAME, DESCRIPTION, DUE_DATE, DUE_TIME)
                .from(TASKS)
                .orderBy(TASK_ID.asc())
                .fetch(record -> new ExportedTask(
                        record.get(NAME),
                        record.get(DESCRIPTION),
                        record.get(DUE_DATE),
                        record.get(DUE_TIME),
                        responsibilities(record.get(TASK_ID))
                ));
    }

    void createImportedTask(String username, ImportedTask task) {
        String taskId = UUID.randomUUID().toString();
        LocalTime dueTime = task.dueDate() == null ? null : task.dueTime();
        List<String> responsibilities = normalizedResponsibilities(task.responsibilityOf());
        dsl.insertInto(TASKS)
                .columns(TASK_ID, NAME, DESCRIPTION, DUE_DATE, DUE_TIME, CREATED_BY)
                .values(taskId, task.name().trim(), task.desc() == null ? "" : task.desc(), task.dueDate(), dueTime, username)
                .execute();
        for (int index = 0; index < responsibilities.size(); index++) {
            dsl.insertInto(TASK_RESPONSIBILITIES)
                    .columns(RESPONSIBILITY_TASK_ID, RESPONSIBLE_NAME, POSITION_INDEX)
                    .values(taskId, responsibilities.get(index), index)
                    .execute();
        }
    }

    private List<String> responsibilities(String taskId) {
        return dsl.select(RESPONSIBLE_NAME)
                .from(TASK_RESPONSIBILITIES)
                .where(RESPONSIBILITY_TASK_ID.eq(taskId))
                .orderBy(POSITION_INDEX.asc())
                .fetch(RESPONSIBLE_NAME);
    }

    private List<String> normalizedResponsibilities(List<String> responsibilities) {
        return responsibilities == null
                ? List.of()
                : responsibilities.stream().filter(value -> value != null).map(String::trim)
                        .filter(value -> !value.isEmpty()).toList();
    }
}
