CREATE TABLE IF NOT EXISTS user_entitlements (
    username VARCHAR(80) NOT NULL,
    entitlement_name VARCHAR(40) NOT NULL,
    PRIMARY KEY (username, entitlement_name),
    CONSTRAINT fk_user_entitlements_user FOREIGN KEY (username) REFERENCES users (username) ON DELETE CASCADE
);
