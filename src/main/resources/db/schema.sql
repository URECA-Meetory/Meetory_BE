-- Meetory Database 초기 설정 (백엔드 JPA validate 기준 스키마)

CREATE USER IF NOT EXISTS 'ureca'@'localhost' IDENTIFIED BY 'ureca';
GRANT ALL PRIVILEGES ON meetorydb.* TO 'ureca'@'localhost';
FLUSH PRIVILEGES;

USE meetorydb;

CREATE TABLE IF NOT EXISTS users (
    id                   BIGINT       NOT NULL AUTO_INCREMENT,
    email                VARCHAR(100) NOT NULL,
    password             VARCHAR(255) NOT NULL,
    nickname             VARCHAR(30)  NOT NULL,
    age                  INT          NULL,
    gender               VARCHAR(20)  NULL,
    hobbies              VARCHAR(255) NULL,
    onboarding_completed TINYINT(1)   NOT NULL DEFAULT 0,
    role                 VARCHAR(20)  NOT NULL,
    created_at           DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS token_blacklist (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    token       VARCHAR(512) NOT NULL,
    expired_at  DATETIME(6)  NOT NULL,
    created_at  DATETIME(6)  NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS team (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    title        VARCHAR(100) NOT NULL,
    category     VARCHAR(20)  NOT NULL,
    description  LONGTEXT     NOT NULL,
    max_members  INT          NOT NULL,
    status       VARCHAR(20)  NOT NULL,
    leader_id    BIGINT       NOT NULL,
    created_at   DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_team_leader FOREIGN KEY (leader_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS member (
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    team_id    BIGINT      NOT NULL,
    user_id    BIGINT      NOT NULL,
    status     VARCHAR(20) NOT NULL,
    joined_at  DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_member_team_user UNIQUE (team_id, user_id),
    CONSTRAINT fk_member_team FOREIGN KEY (team_id) REFERENCES team (id),
    CONSTRAINT fk_member_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS board (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    user_id     BIGINT       NOT NULL,
    title       VARCHAR(200) NOT NULL,
    content     TEXT         NOT NULL,
    created_at  DATETIME(6)  NOT NULL,
    updated_at  DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_board_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS message_thread (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    team_id          BIGINT       NOT NULL,
    starter_id       BIGINT       NOT NULL,
    leader_id        BIGINT       NOT NULL,
    title            VARCHAR(100) NOT NULL,
    created_at       DATETIME     NOT NULL,
    last_message_at  DATETIME     NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_thread_team_starter_leader UNIQUE (team_id, starter_id, leader_id),
    CONSTRAINT fk_thread_team FOREIGN KEY (team_id) REFERENCES team (id),
    CONSTRAINT fk_thread_starter FOREIGN KEY (starter_id) REFERENCES users (id),
    CONSTRAINT fk_thread_leader FOREIGN KEY (leader_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS message_item (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    thread_id   BIGINT       NOT NULL,
    sender_id   BIGINT       NOT NULL,
    content     LONGTEXT     NOT NULL,
    is_read     TINYINT(1)   NOT NULL DEFAULT 0,
    created_at  DATETIME     NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_item_thread FOREIGN KEY (thread_id) REFERENCES message_thread (id),
    CONSTRAINT fk_item_sender FOREIGN KEY (sender_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_message_item_thread ON message_item (thread_id, created_at);
