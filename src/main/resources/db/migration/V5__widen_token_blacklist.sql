-- JWT 전체 문자열 저장 (기존 255자 제한 시 로그아웃/인증 오류 방지)
ALTER TABLE token_blacklist
    MODIFY COLUMN token VARCHAR(512) NOT NULL;
