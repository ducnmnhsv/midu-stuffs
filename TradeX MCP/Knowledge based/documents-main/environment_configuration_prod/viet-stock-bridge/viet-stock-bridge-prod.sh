#!/usr/bin/env bash
keyDir=$TRADEX_WORKING_DIR/keys/lotte/${TRADEX_ENV_DOMAIN}
echo $keyDir
mkdir -p $keyDir

export QUERY_STOCK_NEWS=false
export JOB_FINANCE=true
if [[ "$TRADEX_ENV_NODE_ID" == "1" ]]; then
    if [[ "$TRADEX_ENV_INSTANCE_ID" == "" ]]; then
        export QUERY_STOCK_NEWS=true
        export JOB_FINANCE=false
    fi
    if [[ "$TRADEX_ENV_INSTANCE_ID" != "" && "$TRADEX_ENV_INSTANCE_ID" == "1" ]]; then
        export QUERY_STOCK_NEWS=true
        export JOB_FINANCE=false
    fi
fi

tmp=$(cat << EndOfMessage
    conf.kafkaUrls = process.env.TRADEX_ENV_KAFKA_URLS.split(';');
    conf.redis.host=process.env.TRADEX_ENV_REDIS_HOST;
    conf.domain=process.env.TRADEX_ENV_DOMAIN;
    conf.mongodb.connection.url = \`mongodb://\${process.env.TRADEX_ENV_MONGO_TRADEX_MARKET_USER}:\${process.env.TRADEX_ENV_MONGO_TRADEX_MARKET_PASSWORD}@\${process.env.TRADEX_ENV_MONGO_HOST}:\${process.env.TRADEX_ENV_MONGO_PORT}/tradex-market\`;
    conf.mysql.host = process.env.TRADEX_ENV_MYSQL_HOST;
    conf.mysql.port = process.env.TRADEX_ENV_MYSQL_PORT;
    conf.mysql.user = process.env.TRADEX_ENV_MYSQL_USER;
    conf.mysql.password = process.env.TRADEX_ENV_MYSQL_PASSWORD;
    conf.runQueryStockNews = $QUERY_STOCK_NEWS;
    conf.isRunQuarterFinancialStatement = $JOB_FINANCE;
    conf.isRunYearFinancialStatement = $JOB_FINANCE;
EndOfMessage
)

echo $tmp > $TRADEX_WORKING_DIR/env.js

export NO_PROXY="*.vietstock.vn,*.test.example.com,.example.org"
export http_PROXY=
export HTTP_PROXY=
export HTTPS_PROXY=
export https_PROXY=
export https_proxy=
export http_proxy=