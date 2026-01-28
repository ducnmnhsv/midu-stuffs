#!/usr/bin/env bash

export TRADEX_ENV_MONGO_SERVICE_DB="tradex-market"

enableInitData=false
if [[ "$TRADEX_ENV_INIT_MARKET_BY_REALTIME" != "" ]]; then
  enableInitData=true
fi

tmp=$(cat << EndOfMessage
spring:
  application:
    name: realtime-v2
  data:
    mongodb:
      uri: mongodb://${TRADEX_ENV_MONGO_USER}:${TRADEX_ENV_MONGO_PASSWORD}@${TRADEX_ENV_MONGO_HOST}:${TRADEX_ENV_MONGO_PORT}/tradex-market
    redis:
      host: ${TRADEX_ENV_REDIS_HOST}
      port: ${TRADEX_ENV_REDIS_PORT}
      password: ${TRADEX_ENV_REDIS_PASSWORD}


logging:
  level:
    file: null
    root: WARN
    org.springframework: WARN
    com.techx.tradex: INFO
    in: INFO
    out: INFO

app:
  enableCheckOrderQuote: true
  enableSocketCluster: false
  enableTheme: false
  enableInitData: $enableInitData
  enableSaveStatistic: true
  enableSaveBidOffer: false
  enableSaveQuote: false
  enableSaveQuoteMinute: false
  enableSaveBidAsk: false
  enableSaveWrongOrderQuote: true
  enableQuotePartition: false
  kafkaUrl: "${TRADEX_ENV_KAFKA_PRIMARY_URL}"
  holidayUrl: "http://$MINIO_HOST:9000/mts/holidays.json"
  marketConf:
    symbolStaticBucket: market
    indexName:
      hnx: "HNX"
      upcom: "UPCOM"
      vn: "VN"
    fileConfig:
      defaultType: MINIO
      minio:
        baseUrl: http://$MINIO_HOST:9000
        accessKey: "$MINIO_ACCESS_KEY"
        privateKey: "$MINIO_SECRET_KEY"
        buckets:
          - name: market
  schedulers:
    removeAutoData: "0 55 0 * * MON-FRI"
    clearSymbolDaily: "0 50 22 * * MON-FRI"
    refreshSymbolInfo: "0 35 1 * * MON-FRI"
    saveRedisToDatabase: "0 15,29 10,11,14 * * MON-FRI"
    rollerSymbolInfo: "0 0 21 * * *"
    saveMonitorData: "0 0 10,11 * * MON-FRI"
    resetMonitorData: "0 0 16,17 * * MON-FRI"
    updateThemeStatistic: "0 0 1-9 * * MON-FRI"
    stockTopWorstReturns: "0 5 8 * * MON-FRI"
    restartService: "0 0 0,10,11,23 * * *"
EndOfMessage
)

echo "$tmp" > $TRADEX_WORKING_DIR/application.yml
