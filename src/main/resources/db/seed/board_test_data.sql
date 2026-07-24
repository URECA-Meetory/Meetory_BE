-- 게시판 수동 테스트용 샘플 데이터
-- 사용법: 로그인 가능한 users(id=1)가 있을 때 실행

USE meetorydb;

INSERT INTO board (user_id, title, content, created_at, updated_at)
SELECT 1, '등산 모임 후기', '주말에 북한산 다녀왔어요. 날씨가 좋아서 등산하기 딱이었습니다.', NOW(6), NOW(6)
WHERE EXISTS (SELECT 1 FROM users WHERE id = 1)
  AND NOT EXISTS (SELECT 1 FROM board WHERE title = '등산 모임 후기');

INSERT INTO board (user_id, title, content, created_at, updated_at)
SELECT 1, '독서 모임 추천 도서', '이번 달 추천: ''데미안''. 다음 모임에서 함께 이야기해요.', NOW(6), NOW(6)
WHERE EXISTS (SELECT 1 FROM users WHERE id = 1)
  AND NOT EXISTS (SELECT 1 FROM board WHERE title = '독서 모임 추천 도서');

INSERT INTO board (user_id, title, content, created_at, updated_at)
SELECT 1, '요가 클래스 후기', '초보자도 따라하기 쉬웠고 스트레칭 후 몸이 가벼워졌어요.', NOW(6), NOW(6)
WHERE EXISTS (SELECT 1 FROM users WHERE id = 1)
  AND NOT EXISTS (SELECT 1 FROM board WHERE title = '요가 클래스 후기');
