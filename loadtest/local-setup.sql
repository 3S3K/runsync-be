-- 로컬 부하 테스트용 가상 계정 100개와 러닝룸(진행 중인 협동 러닝) 1개를 만든다. 로컬 DB 에서만 실행한다.
INSERT INTO users (nickname, provider_id, provider, role, is_deleted, birth_date, gender, created_at, updated_at)
SELECT 'lt_' || n, 'loadtest-' || n, 'KAKAO', 'USER', false, DATE '2000-01-01', 'MALE', now(), now()
FROM generate_series(1, 100) AS n;

INSERT INTO art_run_session (title, capacity, meeting_place_name, meeting_point, meeting_time, route_path, status, host_id, created_at, updated_at)
SELECT 'LOADTEST', 100, '부하 테스트', ST_SetSRID(ST_MakePoint(127.4562, 36.3321), 4326), now(),
       ST_GeomFromText('LINESTRING(127.4562 36.3321, 127.4572 36.3331)', 4326), 'IN_PROGRESS',
       (SELECT min(id) FROM users WHERE provider_id LIKE 'loadtest-%'), now(), now();

INSERT INTO art_run_participant (art_run_session_id, user_id, created_at, updated_at)
SELECT (SELECT max(id) FROM art_run_session WHERE title = 'LOADTEST'), id, now(), now()
FROM users WHERE provider_id LIKE 'loadtest-%';

SELECT (SELECT min(id) FROM users WHERE provider_id LIKE 'loadtest-%') AS first_user_id,
       (SELECT count(*) FROM users WHERE provider_id LIKE 'loadtest-%') AS users,
       (SELECT max(id) FROM art_run_session WHERE title = 'LOADTEST') AS room_id,
       -- 스크립트는 first_user_id 부터 번호를 1씩 올려 쓴다. 중간에 다른 계정이 끼면 false 가 나온다
       (SELECT max(id) - min(id) + 1 = count(*) FROM users WHERE provider_id LIKE 'loadtest-%') AS ids_are_consecutive;
