package com.securefromscratch.busybee.task;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

@Repository
class TaskCompletionRepository {

    private static final org.jooq.Table<?> TASKS = DSL.table("tasks");
    private static final org.jooq.Field<String> TASK_ID = DSL.field("task_id", String.class);
    private static final org.jooq.Field<Boolean> DONE = DSL.field("done", Boolean.class);

    private final DSLContext dsl;

    TaskCompletionRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    boolean markDone(String taskId) {
        dsl.update(TASKS)
                .set(DONE, true)
                .where(TASK_ID.eq(taskId))
                .execute();
        return dsl.fetchExists(dsl.selectOne().from(TASKS).where(TASK_ID.eq(taskId)));
    }
}
