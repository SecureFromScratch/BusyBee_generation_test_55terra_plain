package com.securefromscratch.busybee.task;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

@Repository
class TaskRepository {

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

    TaskRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    void create(TaskToCreate task) {
        dsl.insertInto(TASKS)
                .columns(TASK_ID, NAME, DESCRIPTION, DUE_DATE, DUE_TIME, CREATED_BY)
                .values(task.taskId(), task.name(), task.description(), task.dueDate(), task.dueTime(), task.createdBy())
                .execute();

        for (int index = 0; index < task.responsibilityOf().size(); index++) {
            dsl.insertInto(TASK_RESPONSIBILITIES)
                    .columns(RESPONSIBILITY_TASK_ID, RESPONSIBLE_NAME, POSITION_INDEX)
                    .values(task.taskId(), task.responsibilityOf().get(index), index)
                    .execute();
        }
    }
}
