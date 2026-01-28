#!/usr/bin/env bash
unset http_proxy
unset https_proxy
unset HTTP_PROXY
unset HTTPS_PROXY
export NO_PROXY=uat.paave.io
export no_proxy=uat.paave.io

export TRADEX_ENV_ENABLE_ENCRYPT_PASSWORD=true
export TRADEX_ENV_DOMAIN=tradex


tmp=$(cat << EndOfMessage
config.jwt.publicKeyFile = 'jwt-public.key';
config.redis.url = \`redis://${TRADEX_ENV_REDIS_HOST}:${TRADEX_ENV_REDIS_PORT}\`;
config.virtualTrade = {
  baseUrl: 'https://api.paave.io/rest',
  grantType: 'organization_login', 
  organization: 'nhsv',
  clientId: 'nhsv-pro-organization',
  clientSecret: 'XFDzGTMud5Y497CBQVS2Ux',
};
config.port = 4000;
config.cors = {};
config.key = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbnYiOiJkZXYiLCJpYXQiOjE2ODc5NDQwMjIsImV4cCI6MjAwMzMwNDAyMiwiaXNzIjoiRGlmaXNvZnQiLCJzdWIiOiJyZXN0LXByb3h5In0.GYKmR12dPzRy4ZQDVm6nU4FAItjkPliGq1r_t-mVC_GA9G3VnIFAcxJxgflk4Ip4opusMmaEd9suYR7DDR2e-mbEN71NhBjxVQTukH_oWj52LlDgbJUivpmebsYF63w5ZGascCceTzj74gzXe8JHanX9S5UfGpTxXIND9oXiHyeh__cAzecCdpragD8b7xegOZw8bBupZL7_km0o893Dgqa5xeWT5lJDxBY8W6dQBWLJkR9sQZI_nwJ3Ca6RYUEvhVTQYqZZMMelq8zYdQIxXA_knLk-DL5pQez15arpvmqggCgKx1na-yHFLLLv5hZ0C43HI6pvHrs0MKNiz6jeww";
config.datasource.type = 'mysql';
config.datasource.host = process.env.TRADEX_ENV_MYSQL_HOST;
config.datasource.port = 3306;
config.datasource.username = process.env.TRADEX_ENV_MYSQL_USER;
config.datasource.password = process.env.TRADEX_ENV_MYSQL_PASSWORD;
config.datasource.database = 'virtual-trade-proxy';
config.datasource.poolSize = 5;
config.cron.jobRanking = "0 2/30 2-4,6-8 * * MON-FRI"
config.cron.jobRankingCondition = "0 30 8 * * MON-FRI"
config.cron.timeAccessJobRankingCondition = "08:30:00"
config.nhsv.baseUrl = "http://172.33.20.15:3001"
EndOfMessage
)

echo "$tmp" > $TRADEX_WORKING_DIR/env.js

echo "$TRADEX_ENV_JWT_PUBLIC_KEY" > $TRADEX_WORKING_DIR/jwt-public.key
