package com.securefromscratch.busybee.preview;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

@Repository
class TaskPreviewRepository {

    private static final org.jooq.Table<?> TASKS = DSL.table("tasks");
    private static final org.jooq.Field<String> TASK_ID = DSL.field("task_id", String.class);
    private static final org.jooq.Field<String> DESCRIPTION = DSL.field("description", String.class);
    private static final org.jooq.Table<?> LINK_PREVIEWS = DSL.table("link_previews");
    private static final org.jooq.Field<String> PREVIEW_TASK_ID = DSL.field("task_id", String.class);
    private static final org.jooq.Field<String> URL = DSL.field("url", String.class);
    private static final org.jooq.Field<String> PREVIEW_ID = DSL.field("preview_id", String.class);
    private static final org.jooq.Field<String> TITLE = DSL.field("title", String.class);
    private static final org.jooq.Field<String> PREVIEW_DESCRIPTION = DSL.field("description", String.class);
    private static final org.jooq.Field<String> IMAGE_URL = DSL.field("image_url", String.class);
    private static final org.jooq.Field<String> GENERATED_BY = DSL.field("generated_by", String.class);

    private final DSLContext dsl;

    TaskPreviewRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    Optional<String> findTaskDescription(String taskId) {
        return dsl.select(DESCRIPTION)
                .from(TASKS)
                .where(TASK_ID.eq(taskId))
                .fetchOptional(DESCRIPTION);
    }

    void deleteTaskPreview(String taskId, String url) {
        dsl.deleteFrom(LINK_PREVIEWS)
                .where(PREVIEW_TASK_ID.eq(taskId).and(URL.eq(url)))
                .execute();
    }

    void replaceTaskPreview(String taskId, GeneratedTaskPreview preview, String username) {
        deleteTaskPreview(taskId, preview.url());
        dsl.insertInto(LINK_PREVIEWS)
                .columns(PREVIEW_ID, PREVIEW_TASK_ID, URL, TITLE, PREVIEW_DESCRIPTION, IMAGE_URL, GENERATED_BY)
                .values(
                        UUID.randomUUID().toString(),
                        taskId,
                        preview.url(),
                        preview.title(),
                        preview.description(),
                        preview.image(),
                        username
                )
                .execute();
    }

    List<StoredTaskPreview> findTaskPreviews(String taskId) {
        return dsl.select(URL, TITLE, PREVIEW_DESCRIPTION, IMAGE_URL)
                .from(LINK_PREVIEWS)
                .where(PREVIEW_TASK_ID.eq(taskId))
                .orderBy(URL.asc())
                .fetch(record -> new StoredTaskPreview(
                        record.get(URL),
                        record.get(TITLE),
                        record.get(PREVIEW_DESCRIPTION),
                        record.get(IMAGE_URL)
                ));
    }
}
