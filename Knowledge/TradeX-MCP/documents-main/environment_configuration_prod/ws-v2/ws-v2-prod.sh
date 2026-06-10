#!/usr/bin/env bash

export NODE_ENV="production"
mkdir -p $TRADEX_WORKING_DIR/serviceData;

export TX_ENV_CLIENT_ID="nhsv"
export TX_ENV_CLIENT_SECRET="nhsv"
export TRADEX_ENV_MYSQL_SERVICE_DB="tradex-ws"
export TRADEX_ENV_MYSQL_SERVICE_USER="${TRADEX_ENV_MYSQL_TRADEX_WS_USER}"
export TRADEX_ENV_MYSQL_SERVICE_PASSWORD="${TRADEX_ENV_MYSQL_TRADEX_WS_PASSWORD}"

keyDir=$TRADEX_WORKING_DIR/keys/aaa/${TRADEX_ENV_DOMAIN}

mkdir -p $keyDir
export PORT=8001
if [[ $TRADEX_ENV_INSTANCE_ID != "" ]]; then
  export PORT=$((8000 + $TRADEX_ENV_INSTANCE_ID))
fi

tmp=$(cat << EndOfMessage

conf.domain="${TRADEX_ENV_DOMAIN}";
conf.kafkaUrls = ['$TRADEX_ENV_KAFKA_PRIMARY_URL'];
conf.port = $PORT;
conf.i18nNamespaceList = ['message', 'field', 'tuxedo'];
conf.jwt = {
    publicKeyFile: "${keyDir}/jwt-public.key",
    domains:{
        ${TRADEX_ENV_DOMAIN}: {
            publicKeyFile: "${keyDir}/jwt-public.key",
        }
    }
};
conf.isHandlerKafkaV2 = true;
conf.returnSnapshot.enable = true;
conf.redis.url = 'redis://$TRADEX_ENV_REDIS_HOST:$TRADEX_ENV_REDIS_PORT';

EndOfMessage
)
echo "$tmp" > $TRADEX_WORKING_DIR/env.js

tmp=$(cat << EndOfMessage
{
    "clientId": "${TX_ENV_CLIENT_ID}",
    "clientSecret": "${TX_ENV_CLIENT_SECRET}"
}
EndOfMessage
)

echo "$tmp" > $TRADEX_WORKING_DIR/serviceData/wts.json


echo "$TRADEX_ENV_JWT_PUBLIC_KEY" > $keyDir/jwt-public.key
echo "$TRADEX_ENV_PASSWORD_RSA_PUBLIC_KEY" > $keyDir/rsa-public.key
