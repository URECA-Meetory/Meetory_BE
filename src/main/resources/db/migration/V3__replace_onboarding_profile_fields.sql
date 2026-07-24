-- 온보딩 프로필: 포지션/기술스택/소개 → 나이/성별/취미
ALTER TABLE users
    DROP COLUMN position,
    DROP COLUMN tech_stack,
    DROP COLUMN bio;

ALTER TABLE users
    ADD COLUMN age     INT          NULL AFTER nickname,
    ADD COLUMN gender  VARCHAR(20)  NULL AFTER age,
    ADD COLUMN hobbies VARCHAR(255) NULL AFTER gender;
