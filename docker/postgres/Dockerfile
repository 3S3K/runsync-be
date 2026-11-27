# 로컬 개발용 DB: PostgreSQL 16 + PostGIS + pgRouting
FROM postgres:16-bookworm

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        postgresql-16-postgis-3 \
        postgresql-16-postgis-3-scripts \
        postgresql-16-pgrouting \
        postgis \
        osm2pgrouting \
        gdal-bin \
    && rm -rf /var/lib/apt/lists/*
# 빈 볼륨에서 처음 시작할 때 확장을 만든다
COPY init-extensions.sql /docker-entrypoint-initdb.d/
