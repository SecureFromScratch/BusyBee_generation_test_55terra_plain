package com.securefromscratch.busybee.user;

import java.util.List;
import java.util.Optional;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

@Repository
class UserRepository {

    private static final org.jooq.Table<?> USERS = DSL.table("users");
    private static final org.jooq.Field<String> USERNAME = DSL.field("username", String.class);
    private static final org.jooq.Field<String> PASSWORD_HASH = DSL.field("password_hash", String.class);

    private final DSLContext dsl;

    UserRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    void create(String username, String passwordHash) {
        try {
            dsl.insertInto(USERS)
                    .columns(USERNAME, PASSWORD_HASH)
                    .values(username, passwordHash)
                    .execute();
        } catch (DuplicateKeyException exception) {
            throw new DuplicateUsernameException();
        }
    }

    Optional<RegisteredUser> findByUsername(String username) {
        return dsl.select(USERNAME, PASSWORD_HASH)
                .from(USERS)
                .where(USERNAME.eq(username))
                .fetchOptional(record -> new RegisteredUser(
                        record.get(USERNAME),
                        record.get(PASSWORD_HASH)
                ));
    }

    List<String> findUsernamesContaining(String query, int maximumResults) {
        return dsl.select(USERNAME)
                .from(USERS)
                .where(USERNAME.containsIgnoreCase(query))
                .orderBy(USERNAME.asc())
                .limit(maximumResults)
                .fetch(USERNAME);
    }

    int replacePasswordHash(String username, String currentPasswordHash, String replacementPasswordHash) {
        return dsl.update(USERS)
                .set(PASSWORD_HASH, replacementPasswordHash)
                .where(USERNAME.eq(username).and(PASSWORD_HASH.eq(currentPasswordHash)))
                .execute();
    }
}
