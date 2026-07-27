package com.securefromscratch.busybee.comment;

import java.util.Optional;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

@Repository
public class ImageMetadataRepository {

    private static final org.jooq.Table<?> STORED_FILES = DSL.table("stored_files");
    private static final org.jooq.Field<String> FILE_ID = DSL.field("file_id", String.class);
    private static final org.jooq.Field<String> ORIGINAL_NAME = DSL.field("original_name", String.class);
    private static final org.jooq.Field<String> CONTENT_TYPE = DSL.field("content_type", String.class);
    private static final org.jooq.Field<String> FILE_KIND = DSL.field("file_kind", String.class);
    private static final org.jooq.Field<String> STORAGE_NAME = DSL.field("storage_name", String.class);
    private static final org.jooq.Field<String> UPLOADED_BY = DSL.field("uploaded_by", String.class);

    private final DSLContext dsl;

    ImageMetadataRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    void save(StoredImage image, String username) {
        dsl.insertInto(STORED_FILES)
                .columns(FILE_ID, ORIGINAL_NAME, CONTENT_TYPE, FILE_KIND, STORAGE_NAME, UPLOADED_BY)
                .values(image.fileId(), image.originalFilename(), image.contentType(), "IMAGE", image.storageName(), username)
                .execute();
    }

    public Optional<StoredImage> findImage(String fileId) {
        return dsl.select(FILE_ID, ORIGINAL_NAME, CONTENT_TYPE, STORAGE_NAME)
                .from(STORED_FILES)
                .where(FILE_ID.eq(fileId).and(FILE_KIND.eq("IMAGE")))
                .fetchOptional(record -> new StoredImage(
                        record.get(FILE_ID),
                        record.get(ORIGINAL_NAME),
                        record.get(CONTENT_TYPE),
                        record.get(STORAGE_NAME)
                ));
    }
}
