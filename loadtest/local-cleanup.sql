-- 부하 테스트로 생긴 가상 계정·러닝룸·러닝 기록을 지운다. 로컬 DB 에서만 실행한다.
DELETE FROM run_paths WHERE record_id IN (
  SELECT id FROM run_records WHERE user_id IN (SELECT id FROM users WHERE provider_id LIKE 'loadtest-%'));
DELETE FROM run_records WHERE user_id IN (SELECT id FROM users WHERE provider_id LIKE 'loadtest-%');
DELETE FROM running_session WHERE user_id IN (SELECT id FROM users WHERE provider_id LIKE 'loadtest-%');
DELETE FROM art_run_participant WHERE art_run_session_id IN (SELECT id FROM art_run_session WHERE title = 'LOADTEST');
DELETE FROM art_run_session WHERE title = 'LOADTEST';
DELETE FROM users WHERE provider_id LIKE 'loadtest-%';
