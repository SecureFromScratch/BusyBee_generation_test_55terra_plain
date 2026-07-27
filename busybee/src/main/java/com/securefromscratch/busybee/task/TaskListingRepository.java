package com.securefromscratch.busybee.task;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

@Repository
class TaskListingRepository {

    private static final org.jooq.Table<?> TASKS = DSL.table("tasks");
    private static final org.jooq.Field<String> TASK_ID = DSL.field("task_id", String.class);
    private static final org.jooq.Field<String> NAME = DSL.field("name", String.class);
    private static final org.jooq.Field<String> DESCRIPTION = DSL.field("description", String.class);
    private static final org.jooq.Field<LocalDate> DUE_DATE = DSL.field("due_date", LocalDate.class);
    private static final org.jooq.Field<LocalTime> DUE_TIME = DSL.field("due_time", LocalTime.class);
    private static final org.jooq.Field<String> CREATED_BY = DSL.field("created_by", String.class);
    private static final org.jooq.Field<LocalDateTime> CREATED_AT = DSL.field("created_at", LocalDateTime.class);
    private static final org.jooq.Field<Boolean> DONE = DSL.field("done", Boolean.class);
    private static final org.jooq.Table<?> TASK_RESPONSIBILITIES = DSL.table("task_responsibilities");
    private static final org.jooq.Field<String> RESPONSIBILITY_TASK_ID = DSL.field("task_id", String.class);
    private static final org.jooq.Field<String> RESPONSIBLE_NAME = DSL.field("responsible_name", String.class);
    private static final org.jooq.Field<Integer> POSITION_INDEX = DSL.field("position_index", Integer.class);

    private final DSLContext dsl;

    TaskListingRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    List<ListedTask> findAll() {
        return dsl.select(TASK_ID, NAME, DESCRIPTION, DUE_DATE, DUE_TIME, CREATED_BY, CREATED_AT, DONE)
                .from(TASKS)
                .orderBy(CREATED_AT.desc(), TASK_ID.desc())
                .fetch(record -> new ListedTask(
                        record.get(TASK_ID),
                        record.get(NAME),
                        record.get(DESCRIPTION),
                        record.get(DUE_DATE),
                        record.get(DUE_TIME),
                        record.get(CREATED_BY),
                        record.get(CREATED_AT),
                        Boolean.TRUE.equals(record.get(DONE))
                ));
    }

    List<String> findResponsibilities(String taskId) {
        return dsl.select(RESPONSIBLE_NAME)
                .from(TASK_RESPONSIBILITIES)
                .where(RESPONSIBILITY_TASK_ID.eq(taskId))
                .orderBy(POSITION_INDEX.asc())
                .fetch(RESPONSIBLE_NAME);
    }
}
