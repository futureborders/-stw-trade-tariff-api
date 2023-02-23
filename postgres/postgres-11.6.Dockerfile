FROM postgres:11.6
EXPOSE 5432

ENV POSTGRES_USER postgres
ENV POSTGRES_DB signposting
ENV POSTGRES_PASSWORD password

COPY data/create_signposting_schema.sql /data/

COPY scripts/init.sql /docker-entrypoint-initdb.d/
COPY scripts/restore_db.sh /docker-entrypoint-initdb.d/
