#!/usr/bin/env bash

export TX_ENV_EMAIL_ENDPOINT="smtp-mail.outlook.com"
export TX_ENV_EMAIL_PORT="587"
export TX_ENV_EMAIL_SMTP_USER="uat@nhsv.vn"
export TX_ENV_EMAIL_SMTP_PASSWORD="Nhsvuat@082023*"
export TX_ENV_EMAIL_SENDER="uat@nhsv.vn"
export TX_ENV_EMAIL_SUPPORT="uat@nhsv.vn"
export TX_ENV_ONE_SIGNAL_APP_ID="79ea31e2-f4a5-4b16-91b8-02bcadf01564"
export TX_ENV_ONE_SIGNAL_APP_KEY="NGY2NzkzZjctNzBlOC00YjkyLTk5ZDQtZDY4YjFlMWVkNGYy"
export JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF8

tmp=$(cat << EndOfMessage
spring:
  datasource:
    url: jdbc:mysql://${TRADEX_ENV_MYSQL_HOST}:${TRADEX_ENV_MYSQL_PORT}/${TRADEX_ENV_MYSQL_TRADEX_NOTIFICATION_DB}?useSSL=false
    username: ${TRADEX_ENV_MYSQL_TRADEX_NOTIFICATION_USER}
    password: ${TRADEX_ENV_MYSQL_TRADEX_NOTIFICATION_PASSWORD}
    driver-class-name: com.mysql.jdbc.Driver

logging:
  file: /logs/application.log
  level:
    root: WARN
    org.springframework: WARN
    com.techx.ca: INFO
    in: INFO
    out: INFO

app:
  kafkaUrl: \${TRADEX_ENV_KAFKA_PRIMARY_URL}
  kakao: {}
  email:
    endpoint: "${TX_ENV_EMAIL_ENDPOINT}"
    port: ${TX_ENV_EMAIL_PORT}
    username: "${TX_ENV_EMAIL_USERNAME}"
    smtpUsername: "${TX_ENV_EMAIL_SMTP_USER}"
    smtpPassword: "${TX_ENV_EMAIL_SMTP_PASSWORD}"
    sender: "${TX_ENV_EMAIL_SENDER}"
    support: "${TX_ENV_EMAIL_SUPPORT}"
  oneSignal:
    appId: "${TX_ENV_ONE_SIGNAL_APP_ID}"
    apiKey: "${TX_ENV_ONE_SIGNAL_APP_KEY}"
  socketCluster:
    hostname: "${localIp}"
    port: ${TRADEX_ENV_WS_PORT}
    path: "socketcluster/"
    autoReconnection: true
    logMessage: false
  templatesMap: {}
  oneSignalMap:
    nhsv:
      appId: $TX_ENV_ONE_SIGNAL_APP_ID
      apiKey: $TX_ENV_ONE_SIGNAL_APP_KEY
  smsServerMap:
    nhsv:
      url: "https://gbapi.onesms.vn/wspartners/service.asmx"
      type: "ONE_SMS"
      user: "nhsv"
      pass: "NhsvOsms@2023*"
      senderName: "NHSV"
      isFlash: false
      isUnicode: false
      soapAction: "http://1sms.vn/SendMT"
EndOfMessage
)

echo "$tmp" > $TRADEX_WORKING_DIR/application.yml

