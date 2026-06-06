-- Инициализация схемы БД: таблицы tasks, task_attachments и task_tags

CREATE TABLE tasks (
	id BIGSERIAL PRIMARY KEY,
	title VARCHAR(255) NOT NULL,
	description TEXT,
	created_at TIMESTAMP,
	updated_at TIMESTAMP,
	due_date DATE,
	completed BOOLEAN NOT NULL DEFAULT FALSE,
	priority VARCHAR(32)
);

CREATE TABLE task_attachments (
	id BIGSERIAL PRIMARY KEY,
	task_id BIGINT NOT NULL,
	file_name VARCHAR(1024) NOT NULL,
	stored_file_name VARCHAR(1024) NOT NULL,
	content_type VARCHAR(255),
	size BIGINT,
	uploaded_at TIMESTAMP,
	CONSTRAINT fk_task
	  FOREIGN KEY(task_id) REFERENCES tasks(id) ON DELETE CASCADE
);

-- Коллекция тегов для задачи (ElementCollection)
CREATE TABLE task_tags (
	task_id BIGINT NOT NULL,
	tag VARCHAR(255) NOT NULL,
	CONSTRAINT pk_task_tags PRIMARY KEY (task_id, tag),
	CONSTRAINT fk_task_tags_task FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE
);

CREATE INDEX idx_tasks_due_date ON tasks(due_date);
CREATE INDEX idx_tasks_priority ON tasks(priority);


