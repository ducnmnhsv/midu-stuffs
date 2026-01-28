#!/usr/bin/env bash

tmp=$(
  cat <<EndOfMessage

spring:
  application:
    name: copy-trading-engine
  main:
    web-application-type: none
  redis:
    host: ${TRADEX_ENV_REDIS_HOST}
    port: ${TRADEX_ENV_REDIS_PORT}
    password:
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

app:
  kafkaUrl: ${TRADEX_ENV_KAFKA_URLS}
  clusterId: '\${spring.application.name}'
  nodeId: "${TRADEX_ENV_NODE_ID}"
  topics:
    virtualCore: paave-virtual-core-dev
    tuxedo:
      name: "tuxedo"
      maxOrderPerPlace: 500000
      defaultFetchCount: 100
      uri:
        accountInfo: "/api/v1/equity/account/info"
        accountProfitLoss: "/api/v1/equity/account/profitLoss"
        accountAssetInfo: "/api/v1/equity/account/assetInfo"
        accountBanks: "/api/v1/equity/account/banks"
        accountBuyAble: "/api/v1/equity/account/buyable"
        accountSellAble: "/api/v1/equity/account/sellable"
        orderCancel: "/api/v1/equity/order/cancel"
        orderHistory: "/api/v1/equity/order/history"
        placeOrder: "/api/v1/equity/order"
    lotte:
      name: "lotte-bridge"
      maxOrderPerPlace: 500000
      defaultFetchCount: 100
      uri:
        accountInfo: "get:/api/v1/lotte/equity/account/info"
        accountProfitLoss: "get:/api/v1/lotte/equity/account/profitLoss"
        accountAssetInfo: "get:/api/v1/lotte/equity/account/assetInfo"
        accountBanks: "get:/api/v1/lotte/equity/account/banks"
        accountBuyAble: "get:/api/v1/lotte/equity/account/buyable"
        accountSellAble: "get:/api/v1/lotte/equity/account/sellable"
        orderCancel: "put:/api/v1/lotte/equity/order/cancel"
        orderHistory: "get:/api/v1/lotte/equity/order/history"
        placeOrder: "post:/api/v1/lotte/equity/order"
  threadPool:
    threadNamePrefixSet: "CopyTradingEngine-"
    corePoolSize: 10
    maxPoolSize: 50
    queueCapacity: 100
    keepAliveSeconds: 60
    awaitTerminationSeconds: 300
    waitForTasksToCompleteOnShutdown: true
  copyTrading:
    cron: "0 0/30 2-4,6-7 * * *"
  tuxedo:
    topic: "tuxedo"
    maxOrderPerPlace: 500000
    uri:
      accountInfo: "/api/v1/equity/account/info"
      accountProfitLoss: "/api/v1/equity/account/profitLoss"
      accountAssetInfo: "/api/v1/equity/account/assetInfo"
      accountBanks: "/api/v1/equity/account/banks"
      accountBuyAble: "/api/v1/equity/account/buyable"
      accountSellAble: "/api/v1/equity/account/sellable"
      orderCancel: "/api/v1/equity/order/cancel"
      orderHistory: "/api/v1/equity/order/history"
      placeOrder: "/api/v1/equity/order"

feign:
  client:
    config:
      default:
        loggerLevel: full
        connectTimeout: 10000
        readTimeout: 20000

logging:
  file:
    name: /logs/application.log
  level:
    ROOT: INFO
    org.hibernate.SQL: INFO
    com.nhsv.copy.trading: INFO
    org.springframework: INFO
    in: INFO
    out: INFO

server:
  port: 9999

EndOfMessage
)

echo "$tmp" >$TRADEX_WORKING_DIR/application.yml
