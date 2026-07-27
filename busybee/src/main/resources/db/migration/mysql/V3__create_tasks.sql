CREATE TABLE IF NOT EXISTS tasks (
    task_id CHAR(36) NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    due_date DATE NULL,
    due_time TIME NULL,
    created_by VARCHAR(80) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    done BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (task_id),
    CONSTRAINT fk_tasks_created_by FOREIGN KEY (created_by) REFERENCES users (username)
);

CREATE TABLE IF NOT EXISTS task_responsibilities (
    task_id CHAR(36) NOT NULL,
    responsible_name VARCHAR(120) NOT NULL,
    position_index INT NOT NULL,
    PRIMARY KEY (task_id, position_index),
    CONSTRAINT fk_responsibilities_task FOREIGN KEY (task_id) REFERENCES tasks (task_id) ON DELETE CASCADE
);
