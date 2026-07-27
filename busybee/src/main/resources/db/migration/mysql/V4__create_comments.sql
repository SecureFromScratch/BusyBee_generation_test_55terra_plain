CREATE TABLE IF NOT EXISTS comments (
    comment_id CHAR(36) NOT NULL,
    task_id CHAR(36) NOT NULL,
    parent_comment_id CHAR(36) NULL,
    text TEXT NOT NULL,
    indent INT NOT NULL DEFAULT 0,
    created_by VARCHAR(80) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (comment_id),
    CONSTRAINT fk_comments_task FOREIGN KEY (task_id) REFERENCES tasks (task_id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_parent FOREIGN KEY (parent_comment_id) REFERENCES comments (comment_id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_created_by FOREIGN KEY (created_by) REFERENCES users (username)
);
