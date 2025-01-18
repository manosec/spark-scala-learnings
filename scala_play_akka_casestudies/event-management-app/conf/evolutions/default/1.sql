# --- !Ups
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

CREATE TABLE events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    date DATE NOT NULL,
    location VARCHAR(255) NOT NULL,
    organizer_id VARCHAR(255) NOT NULL,
    capacity INT NOT NULL,
    status VARCHAR(20) NOT NULL
);

CREATE TABLE teams (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    team_name VARCHAR(255) NOT NULL,
    team_type VARCHAR(50) NOT NULL,
    team_email VARCHAR(255) NOT NULL,
    team_description TEXT NOT NULL
);

CREATE TABLE tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id BIGINT NOT NULL,
    team_id BIGINT NOT NULL,
    task_description TEXT NOT NULL,
    dead_line VARCHAR(255) NOT NULL,
    special_instructions TEXT,
    status VARCHAR(20) NOT NULL,
    created_at VARCHAR(255) NOT NULL,
    FOREIGN KEY (event_id) REFERENCES events(id)
);

# --- !Downs
DROP TABLE tasks;
DROP TABLE teams;
DROP TABLE events;
DROP TABLE users; 