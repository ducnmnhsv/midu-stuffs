#!/usr/bin/env bash
keyDir=$TRADEX_WORKING_DIR/keys/lotte/${TRADEX_ENV_DOMAIN}
echo $keyDir
mkdir -p $keyDir

if [[ "$TRADEX_ENV_LOTTE_API" == "" ]]; then
    export TRADEX_ENV_LOTTE_API=http://172.33.30.36:8100
fi
if [[ "$TRADEX_ENV_LOTTE_API_KEY" == "" ]]; then
    export TRADEX_ENV_LOTTE_API_KEY=qJxAtDmMuDEan9kUWsY4UuMugFvrYeMe
fi

export ENABLE_JOB=false
if [[ "$TRADEX_ENV_NODE_ID" == "1" ]]; then
    if [[ "$TRADEX_ENV_INSTANCE_ID" == "" ]]; then
        export ENABLE_JOB=true
    fi
    if [[ "$TRADEX_ENV_INSTANCE_ID" != "" && "$TRADEX_ENV_INSTANCE_ID" == "1" ]]; then
        export ENABLE_JOB=true
    fi
fi

tmp=$(cat << EndOfMessage
    conf.kafkaUrls = process.env.TRADEX_ENV_KAFKA_URLS.split(';');
    conf.redis.host=process.env.TRADEX_ENV_REDIS_HOST;
    conf.domain=process.env.TRADEX_ENV_DOMAIN;
    conf.lotte.baseUrl['login'] = '$TRADEX_ENV_LOTTE_API';
    conf.lotte.baseUrl['account'] = '$TRADEX_ENV_LOTTE_API';
    conf.lotte.baseUrl['order'] = '$TRADEX_ENV_LOTTE_API';
    conf.lotte.baseUrl['balance'] = '$TRADEX_ENV_LOTTE_API';
    conf.lotte.baseUrl['ekyc'] = '$TRADEX_ENV_LOTTE_API';
    conf.lotte.baseUrl['market'] = '$TRADEX_ENV_LOTTE_API';
    conf.lotte.baseUrl['notification'] = '$TRADEX_ENV_LOTTE_API';
    conf.lotte.apiKey = '$TRADEX_ENV_LOTTE_API_KEY';
    conf.db.mysql.host = process.env.TRADEX_ENV_MYSQL_HOST;
    conf.db.mysql.port = 3306;
    conf.db.mysql.username = process.env.TRADEX_ENV_MYSQL_USER;
    conf.db.mysql.password = process.env.TRADEX_ENV_MYSQL_PASSWORD;
    conf.db.mysql.database = 'lotte-bridge';
    conf.db.mysql.poolSize = 10;
    conf.enableEncryptPassword=true;
    conf.enableQueryOddLot = $ENABLE_JOB;
EndOfMessage
)

echo "$tmp" > $TRADEX_WORKING_DIR/env.js

echo "$TRADEX_ENV_PASSWORD_RSA_PUBLIC_KEY" > $keyDir/rsa-public.key
echo "$TRADEX_ENV_PASSWORD_RSA_PRIVATE_KEY" > $keyDir/rsa-private.key
