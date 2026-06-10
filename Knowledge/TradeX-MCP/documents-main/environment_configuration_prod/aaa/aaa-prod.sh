#!/usr/bin/env bash

export TX_ENV_CONNECTION_LIMIT="10"
export TRADEX_ENV_MYSQL_SERVICE_DB="tradex-aaa"
export TRADEX_ENV_MYSQL_SERVICE_USER="${TRADEX_ENV_MYSQL_TRADEX_AAA_USER}"
export TRADEX_ENV_MYSQL_SERVICE_PASSWORD="${TRADEX_ENV_MYSQL_TRADEX_AAA_PASSWORD}"
export TRADEX_ENV_BIOMETRIC_VALIDATE_PASSWORD=true
keyDir=$TRADEX_WORKING_DIR/keys/aaa/${TRADEX_ENV_DOMAIN}
echo $keyDir
mkdir -p $keyDir

tmp=$(cat << EndOfMessage
    conf.kafkaUrls = process.env.TRADEX_ENV_KAFKA_URLS.split(';');
    conf.zkUrls = process.env.TRADEX_ENV_ZOOKEEPER_URLS.split(';').map(host => \`\${host}:${TRADEX_ENV_ZOOKEEPER_PORT}\`);
    conf.db.host = process.env.TRADEX_ENV_MYSQL_HOST;
    conf.db.port = process.env.TRADEX_ENV_MYSQL_PORT;
    conf.db.user = process.env.TRADEX_ENV_MYSQL_SERVICE_USER;
    conf.db.password = process.env.TRADEX_ENV_MYSQL_SERVICE_PASSWORD;
    conf.db.database = process.env.TRADEX_ENV_MYSQL_SERVICE_DB;
    conf.db.connectionLimit = $TX_ENV_CONNECTION_LIMIT;
    conf.enableHandleOtp = false;
    conf.redis.url = 'redis://$TRADEX_ENV_REDIS_HOST:$TRADEX_ENV_REDIS_PORT';
    conf.redis.host = '$TRADEX_ENV_REDIS_HOST';
    conf.nhsv.MAX_DAILY_SENT = 500000;
    conf.nhsv.TRIGGER_TIME_INTERVAL = 10;


EndOfMessage
)

echo "$tmp" > $TRADEX_WORKING_DIR/env.js

echo "$TRADEX_ENV_JWT_PUBLIC_KEY" > $keyDir/jwt-public.key
echo "$TRADEX_ENV_JWT_PRIVATE_KEY" > $keyDir/jwt-private.key
echo "$TRADEX_ENV_PASSWORD_RSA_PUBLIC_KEY" > $keyDir/rsa-public.key
echo "$TRADEX_ENV_PASSWORD_RSA_PRIVATE_KEY" > $keyDir/rsa-private.key
