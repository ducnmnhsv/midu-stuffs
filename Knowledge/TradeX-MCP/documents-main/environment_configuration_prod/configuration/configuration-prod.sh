#!/usr/bin/env bash

export TRADEX_ENV_MYSQL_SERVICE_DB="tradex-configuration"
export TRADEX_ENV_MYSQL_SERVICE_USER="${TRADEX_ENV_MYSQL_TRADEX_CONFIGURATION_USER}"
export TRADEX_ENV_MYSQL_SERVICE_PASSWORD="${TRADEX_ENV_MYSQL_TRADEX_CONFIGURATION_PASSWORD}"


tmp=$(cat << EndOfMessage
    conf.kafkaUrls = process.env.TRADEX_ENV_KAFKA_URLS.split(';');
    conf.db.connection.host = process.env.TRADEX_ENV_MYSQL_HOST;
    conf.db.connection.port = process.env.TRADEX_ENV_MYSQL_PORT;
    conf.db.connection.user = process.env.TRADEX_ENV_MYSQL_SERVICE_USER;
    conf.db.connection.password = process.env.TRADEX_ENV_MYSQL_SERVICE_PASSWORD;
    conf.db.connection.database = process.env.TRADEX_ENV_MYSQL_SERVICE_DB;
    conf.storageService = "minio";
    conf.minio.external = {
        endPoint: 'nhsvbackendpro.nhsv.vn',
        useSSL: true,
        accessKey: '$MINIO_ACCESS_KEY',
        secretKey: '$MINIO_SECRET_KEY',
    };
    conf.minio.internal = {
        endPoint: '$TRADEX_ENV_MINIO_HOST',
        port: 9000,
        useSSL: false,
        accessKey: '$MINIO_ACCESS_KEY',
        secretKey: '$MINIO_SECRET_KEY',
    };
EndOfMessage
)

echo "$tmp" > $TRADEX_WORKING_DIR/env.js