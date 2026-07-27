CREATE TABLE IF NOT EXISTS task_comment_summaries (
    task_id CHAR(36) NOT NULL,
    summary TEXT NOT NULL,
    summarized_comment_count INT NOT NULL,
    summarized_latest_comment_at TIMESTAMP NULL,
    generated_by VARCHAR(80) NOT NULL,
    credential_source VARCHAR(20) NOT NULL,
    generated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (task_id),
    CONSTRAINT fk_task_comment_summaries_task FOREIGN KEY (task_id) REFERENCES tasks (task_id) ON DELETE CASCADE,
    CONSTRAINT fk_task_comment_summaries_user FOREIGN KEY (generated_by) REFERENCES users (username)
);
