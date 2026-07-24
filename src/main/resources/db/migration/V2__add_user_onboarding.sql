-- 신규 DB: 온보딩 프로필 필드 (나이/성별/취미)
ALTER TABLE users
    ADD COLUMN age                 INT          NULL AFTER nickname,
    ADD COLUMN gender              VARCHAR(20)  NULL AFTER age,
    ADD COLUMN hobbies             VARCHAR(255) NULL AFTER gender,
    ADD COLUMN onboarding_completed TINYINT(1)  NOT NULL DEFAULT 0 AFTER hobbies;
