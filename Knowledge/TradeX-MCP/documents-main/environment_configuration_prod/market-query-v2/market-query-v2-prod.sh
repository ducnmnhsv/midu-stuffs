#!/usr/bin/env bash
tmp=$(cat << EndOfMessage

conf.kafkaUrls = process.env.TRADEX_ENV_KAFKA_URLS.split(';');
conf.redis.url = 'redis://${TRADEX_ENV_REDIS_HOST}:${TRADEX_ENV_REDIS_PORT}';
conf.db.connection.url = 'mongodb://${TRADEX_ENV_MONGO_TRADEX_MARKET_USER}:${TRADEX_ENV_MONGO_TRADEX_MARKET_PASSWORD}@${TRADEX_ENV_MONGO_HOST}:${TRADEX_ENV_MONGO_PORT}/tradex-market';
conf.domain = process.env.TRADEX_ENV_DOMAIN;
conf.holidays = [
  "20240208",
  "20240209",
  "20240210",
  "20240211",
  "20240212",
  "20240213",
  "20240214"
];
console.log(conf);

EndOfMessage
)

echo "$tmp" > $TRADEX_WORKING_DIR/env.js
