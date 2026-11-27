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