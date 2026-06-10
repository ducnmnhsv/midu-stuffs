#!/usr/bin/env bash

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
server:
  port: $TRADEX_ENV_NSHV_ADMIN_PORT
spring:
  application:
    name: nhsv-admin
  datasource:
    type: com.zaxxer.hikari.HikariDataSource
    url: jdbc:mysql://${TRADEX_ENV_MYSQL_HOST}:${TRADEX_ENV_MYSQL_PORT}/nhsv-admin?useUnicode=true&characterEncoding=utf8&useSSL=false&useLegacyDatetimeCode=false&serverTimezone=UTC&createDatabaseIfNotExist=true
    username: ${TRADEX_ENV_MYSQL_USER}
    password: ${TRADEX_ENV_MYSQL_PASSWORD}
    driver-class-name: com.mysql.jdbc.Driver
    hikari:
      poolName: Hikari
      auto-commit: false
      data-source-properties:
        cachePrepStmts: true
        prepStmtCacheSize: 250
        prepStmtCacheSqlLimit: 2048
        useServerPrepStmts: true
  jackson:
    serialization:
      indent-output: true
  mail:
    host: smtp-mail.outlook.com
    port: 587
    username: itech@nhsv.vn
    password: Abc123@@
    protocol: smtp
    tls: true
    properties.mail.smtp:
      auth: true
      starttls.enable: true
      ssl.trust: smtp-mail.outlook.com
  jmx:
    enabled: false
  redis:
    host: ${TRADEX_ENV_REDIS_HOST}
    port: ${TRADEX_ENV_REDIS_PORT}
    password:
  data:
    mongodb:
      uri: "mongodb://${TRADEX_ENV_MONGO_TRADEX_MARKET_USER}:${TRADEX_ENV_MONGO_TRADEX_MARKET_PASSWORD}@${TRADEX_ENV_MONGO_HOST}:${TRADEX_ENV_MONGO_PORT}/tradex-market"


jhipster:
  clientApp:
    name: 'NHSVAdminApp'
  security:
    content-security-policy: "default-src 'self'; frame-src 'self' data:; script-src 'self' 'unsafe-inline' 'unsafe-eval' https://storage.googleapis.com; style-src 'self' https://fonts.googleapis.com 'unsafe-inline'; img-src * 'self' blob: data: https:; font-src 'self' https://fonts.gstatic.com data:"
  mail:
    from: itech@nhsv.vn
    base-url: https://admin.nhsvbackendpro.nhsv.vn


application:
  kafkaUrls: ${TRADEX_ENV_KAFKA_URLS}
  clusterId: '\${spring.application.name}'
  nodeId: "${TRADEX_ENV_NODE_ID}"
  supportEmail: 'support@nhsv.vn'
  enableJob: "${ENABLE_JOB}"
  fileConf:
    defaultType: MINIO
    minio:
        baseUrl: "https://nhsvbackendpro.nhsv.vn/"
        urlRewriteTo: "https://nhsvbackendpro.nhsv.vn/"
        accessKey: "$MINIO_ACCESS_KEY"
        privateKey: "$MINIO_SECRET_KEY"
        buckets:
          - name: nhsv-admin

logging:
  file:
    name: /logs/application.log
  level:
    ROOT: INFO
    tech.jhipster: INFO
    org.hibernate.SQL: INFO
    com.difisoft.nhsv.admin: INFO
app:
  cron:
    daily-profit-loss: 0 30 08 * * *
    daily-total-subscribers: 0 00 09 * * *
  kafka:
    internal:
      copy-trading-engine:
        topic: copy-trading-engine
        uri:
          trigger-portfolio-changed: "get:/api/v1/copyTrading/portfolioChanged/trigger"
  smsServer:
    url: "https://gbapi.onesms.vn/wspartners/service.asmx?op=SendMT"
    type: "ONE_SMS"
    user: "nhsv"
    pass: "NhsvOsms@2023*"
    senderName: "NHSV"
    isFlash: false
    isUnicode: false
    soapAction: "http://1sms.vn/SendMT"
  rootURL: http://172.33.30.36:8100
  nhsvConfig:
    headers:
      api-key: "qJxAtDmMuDEan9kUWsY4UuMugFvrYeMe"


EndOfMessage
)

echo "$tmp" > $TRADEX_WORKING_DIR/application.yml
