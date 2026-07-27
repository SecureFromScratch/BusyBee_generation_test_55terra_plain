package com.securefromscratch.busybee.settings;

import java.util.Optional;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

@Repository
class UserSettingsRepository {

    private static final org.jooq.Table<?> USER_SETTINGS = DSL.table("user_settings");
    private static final org.jooq.Field<String> USERNAME = DSL.field("username", String.class);
    private static final org.jooq.Field<Integer> SUMMARY_THRESHOLD_COMMENTS =
            DSL.field("summary_threshold_comments", Integer.class);

    private final DSLContext dsl;

    UserSettingsRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    Optional<Integer> findSummaryThreshold(String username) {
        return dsl.select(SUMMARY_THRESHOLD_COMMENTS)
                .from(USER_SETTINGS)
                .where(USERNAME.eq(username))
                .fetchOptional(SUMMARY_THRESHOLD_COMMENTS);
    }

    void saveSummaryThreshold(String username, int summaryThresholdComments) {
        dsl.insertInto(USER_SETTINGS)
                .columns(USERNAME, SUMMARY_THRESHOLD_COMMENTS)
                .values(username, summaryThresholdComments)
                .onDuplicateKeyUpdate()
                .set(SUMMARY_THRESHOLD_COMMENTS, summaryThresholdComments)
                .execute();
    }
}
