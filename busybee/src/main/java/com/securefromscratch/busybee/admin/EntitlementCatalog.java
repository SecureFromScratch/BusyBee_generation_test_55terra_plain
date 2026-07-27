package com.securefromscratch.busybee.admin;

import java.util.List;
import java.util.Set;

final class EntitlementCatalog {

    static final List<String> ALL = List.of(
            "IMPORT_ENABLED",
            "EXPORT_ENABLED",
            "PAID_LEVEL_1",
            "AI_ENABLED",
            "OCR_ENABLED"
    );
    static final Set<String> SET = Set.copyOf(ALL);

    private EntitlementCatalog() {
    }
}
