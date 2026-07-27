package com.securefromscratch.busybee.comment;

import java.time.LocalDateTime;
import java.util.List;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

@Repository
class CommentRepository {

    private static final org.jooq.Table<?> TASKS = DSL.table("tasks");
    private static final org.jooq.Field<String> TASK_ID = DSL.field("task_id", String.class);
    private static final org.jooq.Table<?> COMMENTS = DSL.table("comments");
    private static final org.jooq.Field<String> COMMENT_ID = DSL.field("comment_id", String.class);
    private static final org.jooq.Field<String> COMMENT_TASK_ID = DSL.field("task_id", String.class);
    private static final org.jooq.Field<String> PARENT_COMMENT_ID = DSL.field("parent_comment_id", String.class);
    private static final org.jooq.Field<String> TEXT = DSL.field("text", String.class);
    private static final org.jooq.Field<String> IMAGE_FILE_ID = DSL.field("image_file_id", String.class);
    private static final org.jooq.Field<String> ATTACHMENT_FILE_ID = DSL.field("attachment_file_id", String.class);
    private static final org.jooq.Field<Integer> INDENT = DSL.field("indent", Integer.class);
    private static final org.jooq.Field<String> CREATED_BY = DSL.field("created_by", String.class);
    private static final org.jooq.Field<LocalDateTime> CREATED_AT = DSL.field("created_at", LocalDateTime.class);

    private final DSLContext dsl;

    CommentRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    boolean taskExists(String taskId) {
        return dsl.fetchExists(dsl.selectOne().from(TASKS).where(TASK_ID.eq(taskId)));
    }

    void create(
            String commentId,
            String taskId,
            String parentCommentId,
            String text,
            String imageId,
            String attachmentId,
            int indent,
            String username
    ) {
        dsl.insertInto(COMMENTS)
                .columns(
                        COMMENT_ID,
                        COMMENT_TASK_ID,
                        PARENT_COMMENT_ID,
                        TEXT,
                        IMAGE_FILE_ID,
                        ATTACHMENT_FILE_ID,
                        INDENT,
                        CREATED_BY
                )
                .values(commentId, taskId, parentCommentId, text, imageId, attachmentId, indent, username)
                .execute();
    }

    java.util.Optional<CommentParent> findParent(String commentId) {
        return dsl.select(COMMENT_ID, COMMENT_TASK_ID, INDENT)
                .from(COMMENTS)
                .where(COMMENT_ID.eq(commentId))
                .fetchOptional(record -> new CommentParent(
                        record.get(COMMENT_ID),
                        record.get(COMMENT_TASK_ID),
                        record.get(INDENT)
                ));
    }

    List<StoredComment> findByTaskId(String taskId) {
        return dsl.select(COMMENT_ID, TEXT, IMAGE_FILE_ID, ATTACHMENT_FILE_ID, INDENT, CREATED_BY, CREATED_AT)
                .from(COMMENTS)
                .where(COMMENT_TASK_ID.eq(taskId))
                .orderBy(CREATED_AT.asc(), COMMENT_ID.asc())
                .fetch(record -> new StoredComment(
                        record.get(COMMENT_ID),
                        record.get(TEXT),
                        record.get(IMAGE_FILE_ID),
                        record.get(ATTACHMENT_FILE_ID),
                        record.get(INDENT),
                        record.get(CREATED_BY),
                        record.get(CREATED_AT)
                ));
    }
}
