CREATE TABLE IF NOT EXISTS link_previews (
    preview_id CHAR(36) NOT NULL,
    task_id CHAR(36) NULL,
    comment_id CHAR(36) NULL,
    url VARCHAR(2048) NOT NULL,
    title VARCHAR(500) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    image_url VARCHAR(2048) NULL,
    generated_by VARCHAR(80) NOT NULL,
    generated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (preview_id),
    CONSTRAINT fk_link_previews_task FOREIGN KEY (task_id) REFERENCES tasks (task_id) ON DELETE CASCADE,
    CONSTRAINT fk_link_previews_comment FOREIGN KEY (comment_id) REFERENCES comments (comment_id) ON DELETE CASCADE,
    CONSTRAINT fk_link_previews_generated_by FOREIGN KEY (generated_by) REFERENCES users (username),
    CONSTRAINT chk_link_previews_owner CHECK (
        (task_id IS NOT NULL AND comment_id IS NULL) OR (task_id IS NULL AND comment_id IS NOT NULL)
    )
);
