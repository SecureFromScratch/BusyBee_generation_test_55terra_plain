CREATE TABLE IF NOT EXISTS stored_files (
    file_id CHAR(36) NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(120) NOT NULL,
    file_kind VARCHAR(20) NOT NULL,
    storage_name VARCHAR(80) NOT NULL,
    uploaded_by VARCHAR(80) NOT NULL,
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (file_id),
    CONSTRAINT fk_stored_files_uploaded_by FOREIGN KEY (uploaded_by) REFERENCES users (username)
);

ALTER TABLE comments ADD COLUMN image_file_id CHAR(36) NULL;

ALTER TABLE comments ADD CONSTRAINT fk_comments_image
    FOREIGN KEY (image_file_id) REFERENCES stored_files (file_id);
