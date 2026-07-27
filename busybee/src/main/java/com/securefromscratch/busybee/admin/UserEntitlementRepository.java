package com.securefromscratch.busybee.admin;

import java.util.List;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

@Repository
class UserEntitlementRepository {

    private static final org.jooq.Table<?> USERS = DSL.table("users");
    private static final org.jooq.Field<String> USERNAME = DSL.field("username", String.class);
    private static final org.jooq.Table<?> USER_ENTITLEMENTS = DSL.table("user_entitlements");
    private static final org.jooq.Field<String> ENTITLEMENT_USERNAME = DSL.field("username", String.class);
    private static final org.jooq.Field<String> ENTITLEMENT = DSL.field("entitlement_name", String.class);

    private final DSLContext dsl;

    UserEntitlementRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    boolean userExists(String username) {
        return dsl.fetchExists(dsl.selectOne().from(USERS).where(USERNAME.eq(username)));
    }

    List<String> findUsernames(String query) {
        return dsl.select(USERNAME)
                .from(USERS)
                .where(USERNAME.containsIgnoreCase(query))
                .orderBy(USERNAME.asc())
                .limit(50)
                .fetch(USERNAME);
    }

    List<String> findEntitlements(String username) {
        return dsl.select(ENTITLEMENT)
                .from(USER_ENTITLEMENTS)
                .where(ENTITLEMENT_USERNAME.eq(username))
                .orderBy(ENTITLEMENT.asc())
                .fetch(ENTITLEMENT);
    }

    void replaceEntitlements(String username, List<String> entitlements) {
        dsl.deleteFrom(USER_ENTITLEMENTS)
                .where(ENTITLEMENT_USERNAME.eq(username))
                .execute();
        for (String entitlement : entitlements) {
            dsl.insertInto(USER_ENTITLEMENTS)
                    .columns(ENTITLEMENT_USERNAME, ENTITLEMENT)
                    .values(username, entitlement)
                    .execute();
        }
    }
}
