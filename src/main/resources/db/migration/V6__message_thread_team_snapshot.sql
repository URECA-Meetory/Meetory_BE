-- 모임 삭제 후에도 쪽지함에 모임명을 남기기 위해 team_id nullable + 스냅샷 추가
ALTER TABLE message_thread
    ADD COLUMN team_title_snapshot VARCHAR(100) NULL AFTER title,
    MODIFY COLUMN team_id BIGINT NULL;
