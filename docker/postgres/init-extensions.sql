-- 빈 볼륨으로 DB를 처음 만들 때 한 번 실행된다.
-- 엔티티의 geometry 컬럼에 postgis가, 추천 서버의 routing 스키마에 pgrouting과 postgis_raster가 필요하다.
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS postgis_raster;
CREATE EXTENSION IF NOT EXISTS pgrouting;
