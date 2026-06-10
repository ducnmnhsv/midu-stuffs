#!/usr/bin/env bash

if [[ "$TRADEX_ENV_LOTTE_WS" == "" ]]; then
    export TRADEX_ENV_LOTTE_WS=ws://172.33.30.36:9900
fi

tmp=$(cat << EndOfMessage
    conf.kafkaUrls = process.env.TRADEX_ENV_KAFKA_URLS.split(';');
    conf.lotte.websocketAddress = '$TRADEX_ENV_LOTTE_WS';
EndOfMessage
)
echo $tmp > $TRADEX_WORKING_DIR/env.js
