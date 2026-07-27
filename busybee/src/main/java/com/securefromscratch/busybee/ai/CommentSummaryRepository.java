package com.securefromscratch.busybee.ai;

import java.time.LocalDateTime;
import java.util.Optional;

import org.jooq.DSLContext;
import org.jooq.Record2;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

@Repository
class CommentSummaryRepository {

    private static final org.jooq.Table<?> TASKS = DSL.table("tasks");
    private static final org.jooq.Field<String> TASK_ID = DSL.field("task_id", String.class);
    private static final org.jooq.Table<?> COMMENTS = DSL.table("comments");
    private static final org.jooq.Field<String> COMMENT_TASK_ID = DSL.field("task_id", String.class);
    private static final org.jooq.Field<String> IMAGE_FILE_ID = DSL.field("image_file_id", String.class);
    private static final org.jooq.Field<LocalDateTime> COMMENT_CREATED_AT = DSL.field("created_at", LocalDateTime.class);
    private static final org.jooq.Table<?> SUMMARIES = DSL.table("task_comment_summaries");
    private static final org.jooq.Field<String> SUMMARY_TASK_ID = DSL.field("task_id", String.class);
    private static final org.jooq.Field<String> SUMMARY = DSL.field("summary", String.class);
    private static final org.jooq.Field<Integer> SUMMARIZED_COUNT = DSL.field("summarized_comment_count", Integer.class);
    private static final org.jooq.Field<LocalDateTime> SUMMARIZED_LATEST =
            DSL.field("summarized_latest_comment_at", LocalDateTime.class);
    private static final org.jooq.Field<String> GENERATED_BY = DSL.field("generated_by", String.class);
    private static final org.jooq.Field<String> CREDENTIAL_SOURCE = DSL.field("credential_source", String.class);
    private static final org.jooq.Field<LocalDateTime> GENERATED_AT = DSL.field("generated_at", LocalDateTime.class);

    private final DSLContext dsl;

    CommentSummaryRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    boolean taskExists(String taskId) {
        return dsl.fetchExists(dsl.selectOne().from(TASKS).where(TASK_ID.eq(taskId)));
    }

    CommentStatistics commentStatistics(String taskId) {
        Record2<Integer, LocalDateTime> record = dsl.select(DSL.count(), DSL.max(COMMENT_CREATED_AT))
                .from(COMMENTS)
                .where(COMMENT_TASK_ID.eq(taskId).and(IMAGE_FILE_ID.isNull()))
                .fetchOne();
        return new CommentStatistics(record.value1(), record.value2());
    }

    Optional<StoredCommentSummary> findSummary(String taskId) {
        return dsl.select(SUMMARY, SUMMARIZED_COUNT, SUMMARIZED_LATEST, GENERATED_BY, CREDENTIAL_SOURCE, GENERATED_AT)
                .from(SUMMARIES)
                .where(SUMMARY_TASK_ID.eq(taskId))
                .fetchOptional(record -> new StoredCommentSummary(
                        record.get(SUMMARY),
                        record.get(SUMMARIZED_COUNT),
                        record.get(SUMMARIZED_LATEST),
                        record.get(GENERATED_BY),
                        record.get(CREDENTIAL_SOURCE),
                        record.get(GENERATED_AT)
                ));
    }

    void save(String taskId, String summary, CommentStatistics statistics, String username, String credentialSource) {
        LocalDateTime generatedAt = LocalDateTime.now();
        dsl.insertInto(SUMMARIES)
                .columns(
                        SUMMARY_TASK_ID,
                        SUMMARY,
                        SUMMARIZED_COUNT,
                        SUMMARIZED_LATEST,
                        GENERATED_BY,
                        CREDENTIAL_SOURCE,
                        GENERATED_AT
                )
                .values(
                        taskId,
                        summary,
                        statistics.count(),
                        statistics.latestCommentAt(),
                        username,
                        credentialSource,
                        generatedAt
                )
                .onDuplicateKeyUpdate()
                .set(SUMMARY, summary)
                .set(SUMMARIZED_COUNT, statistics.count())
                .set(SUMMARIZED_LATEST, statistics.latestCommentAt())
                .set(GENERATED_BY, username)
                .set(CREDENTIAL_SOURCE, credentialSource)
                .set(GENERATED_AT, generatedAt)
                .execute();
    }
}
