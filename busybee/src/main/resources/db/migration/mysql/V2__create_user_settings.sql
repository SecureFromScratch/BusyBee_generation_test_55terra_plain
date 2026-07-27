CREATE TABLE IF NOT EXISTS user_settings (
    username VARCHAR(80) NOT NULL,
    summary_threshold_comments INT NOT NULL DEFAULT 5,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (username),
    CONSTRAINT fk_user_settings_username FOREIGN KEY (username) REFERENCES users (username) ON DELETE CASCADE,
    CONSTRAINT chk_user_settings_summary_threshold_comments
        CHECK (summary_threshold_comments BETWEEN 5 AND 15)
);
