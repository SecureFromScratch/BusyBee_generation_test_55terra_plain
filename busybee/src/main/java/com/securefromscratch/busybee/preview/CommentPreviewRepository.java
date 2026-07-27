package com.securefromscratch.busybee.preview;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

@Repository
class CommentPreviewRepository {

    private static final org.jooq.Table<?> COMMENTS = DSL.table("comments");
    private static final org.jooq.Field<String> COMMENT_ID = DSL.field("comment_id", String.class);
    private static final org.jooq.Field<String> COMMENT_TEXT = DSL.field("text", String.class);
    private static final org.jooq.Table<?> LINK_PREVIEWS = DSL.table("link_previews");
    private static final org.jooq.Field<String> PREVIEW_ID = DSL.field("preview_id", String.class);
    private static final org.jooq.Field<String> PREVIEW_COMMENT_ID = DSL.field("comment_id", String.class);
    private static final org.jooq.Field<String> URL = DSL.field("url", String.class);
    private static final org.jooq.Field<String> TITLE = DSL.field("title", String.class);
    private static final org.jooq.Field<String> DESCRIPTION = DSL.field("description", String.class);
    private static final org.jooq.Field<String> IMAGE_URL = DSL.field("image_url", String.class);
    private static final org.jooq.Field<String> GENERATED_BY = DSL.field("generated_by", String.class);

    private final DSLContext dsl;

    CommentPreviewRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    Optional<String> findCommentText(String commentId) {
        return dsl.select(COMMENT_TEXT)
                .from(COMMENTS)
                .where(COMMENT_ID.eq(commentId))
                .fetchOptional(COMMENT_TEXT);
    }

    void replaceCommentPreview(String commentId, GeneratedTaskPreview preview, String username) {
        deleteCommentPreview(commentId, preview.url());
        dsl.insertInto(LINK_PREVIEWS)
                .columns(PREVIEW_ID, PREVIEW_COMMENT_ID, URL, TITLE, DESCRIPTION, IMAGE_URL, GENERATED_BY)
                .values(
                        UUID.randomUUID().toString(),
                        commentId,
                        preview.url(),
                        preview.title(),
                        preview.description(),
                        preview.image(),
                        username
                )
                .execute();
    }

    void deleteCommentPreview(String commentId, String url) {
        dsl.deleteFrom(LINK_PREVIEWS)
                .where(PREVIEW_COMMENT_ID.eq(commentId).and(URL.eq(url)))
                .execute();
    }

    List<StoredCommentPreview> findCommentPreviews(String commentId) {
        return dsl.select(URL, TITLE, DESCRIPTION, IMAGE_URL)
                .from(LINK_PREVIEWS)
                .where(PREVIEW_COMMENT_ID.eq(commentId))
                .orderBy(URL.asc())
                .fetch(record -> new StoredCommentPreview(
                        record.get(URL),
                        record.get(TITLE),
                        record.get(DESCRIPTION),
                        record.get(IMAGE_URL)
                ));
    }
}
