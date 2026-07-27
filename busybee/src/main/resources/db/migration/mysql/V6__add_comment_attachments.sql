ALTER TABLE comments ADD COLUMN attachment_file_id CHAR(36) NULL;

ALTER TABLE comments ADD CONSTRAINT fk_comments_attachment
    FOREIGN KEY (attachment_file_id) REFERENCES stored_files (file_id);
