#!/usr/bin/env bash

keyDir=$TRADEX_WORKING_DIR/keys/ekyc/fpt/
echo $keyDir
mkdir -p $keyDir
keyDirVnpt=$TRADEX_WORKING_DIR/keys/ekyc/vnpt/
echo $keyDirVnpt
mkdir -p $keyDirVnpt

if [[ "$TRADEX_ENV_LOTTE_API" == "" ]]; then
    export TRADEX_ENV_LOTTE_API=http://172.33.30.36:8100
fi
if [[ "$TRADEX_ENV_LOTTE_API_KEY" == "" ]]; then
    export TRADEX_ENV_LOTTE_API_KEY=qJxAtDmMuDEan9kUWsY4UuMugFvrYeMe
fi

export TRADEX_ENV_MYSQL_TRADEX_EKYC_ADMIN_DB=ekyc_admin

tmp=$(cat << EndOfMessage
spring:
  datasource:
    url: jdbc:mysql://${TRADEX_ENV_MYSQL_HOST}:${TRADEX_ENV_MYSQL_PORT}/${TRADEX_ENV_MYSQL_TRADEX_EKYC_ADMIN_DB}?useUnicode=true&characterEncoding=utf8&useSSL=false&useLegacyDatetimeCode=false&serverTimezone=UTC&createDatabaseIfNotExist=true
    username: ${TRADEX_ENV_MYSQL_USER}
    password: ${TRADEX_ENV_MYSQL_PASSWORD}
  redis:
    host: ${TRADEX_ENV_REDIS_HOST}
    port: ${TRADEX_ENV_REDIS_PORT}
    #password: ${TRADEX_ENV_REDIS_PASSWORD}
#  profiles:
#    include: no-liquibase
  mail:
    host: 172.33.20.33
    port: 587
    username: ekyc@nhsv.vn
    password: NhsveKYC@102021*
    protocol: smtp
    tls: true
    debug: true
    properties.mail.smtp:
      auth: true
      starttls.enable: true
      ssl.trust: 172.33.20.33
    mime:
      encodeparameters: false
      allowutf8: false

server:
  port: $TRADEX_ENV_EKYC_ADMIN_PORT

logging:
  charset:
    console: UTF-8
    file: UTF-8
  file:
    name: /logs/application.log
  level:
    ROOT: INFO
    tech.jhipster: INFO
    org.hibernate.SQL: INFO
    com.techx.tradex.ekycadmin: INFO

jhipster:
  mail:
    from: ekyc@nhsv.vn
  security:
    content-security-policy: "default-src 'self'; frame-src 'self' data:; script-src 'self' 'unsafe-inline' 'unsafe-eval' https://storage.googleapis.com; style-src 'self' https://fonts.googleapis.com 'unsafe-inline'; img-src * 'self' data:; font-src 'self' https://fonts.gstatic.com data:"

app:
  kafkaUrl: "${TRADEX_ENV_KAFKA_PRIMARY_URL}"
  enableAddSignatureToCore: true
  enableCallTllOpenAccount: true
  enableRequireGender: true
  enableDebugPlaceIssueMapping: false
  matchThresholdPercentToCallTllOpenAccount: 80.0
  defaultFileName: false
  keyPath: $keyDir/private.pem
  otpLength: 6
  otpGenTime: 5
  otpMaxGenTime: 5
  otpKeyLifeTime: 86400
  resizeSignature:
    width: 250
    heigth: 250
    quality: 0.6
  otpLifeTime:
    sms:
      REGISTER: 180
      RESET_PASSWORD: 180
      NEW_DEVICE: 180
      E_KYC: 180
      DEFAULT_EXPIRE_TIME: 180
    email:
      REGISTER: 900
      RESET_PASSWORD: 900
      NEW_DEVICE: 900
      E_KYC: 900
      DEFAULT_EXPIRE_TIME: 900
  lotteConfig:
    apiKey: "$TRADEX_ENV_LOTTE_API_KEY"
    rootUrl: "$TRADEX_ENV_LOTTE_API"
    createAccountUrl: "#{rootUrl}/tsol/apikey/tuxsvc/ekyc/create-account"
    updateAccountUrl: "#{rootUrl}/tsol/apikey/tuxsvc/ekyc/update-account"
    uploadImageUrl: "#{rootUrl}/tsol/apikey/fileupl/eky-image"
    responseCodeSuccess: "0000"
    responseErrorCodeBusiness: "1005"
  feignClient:
    lotteApi:
      name: 'lotte-api'
      host: '$TRADEX_ENV_LOTTE_API/tsol/apikey'
      apiKey: '$TRADEX_ENV_LOTTE_API_KEY'
    fpt:
      eContract:
        name: "fptEcontract"
        host: "https://econtract.fpt.com/app"
        loginInfo:
          username: "support@nhsv.vn"
          password: "J2cDdKvQ1nz"
          clientId: "fpt_econtract_nhsv_integrate"
          clientSecret: "fpt_econtract_nhsv_integrate"
        template:
          alias:
            hdmtk: "HDMTK"
  vnpt:
    publicKey: $keyDirVnpt/public.pem
    algorithmSignature: "SHA256withRSA"
  threadPool:
    threadNamePrefixSet: 'eKyc-pool-'
    corePoolSize: 100
    queueCapacity: 5
    maxPoolSize: 500
    keepAliveSeconds: 120
    awaitTerminationSeconds: 300
    waitForTasksToCompleteOnShutdown: true
    # scheduler
    schedulerThreadNamePrefixSet: 'scheduler-eKyc-pool-'
    schedulerPoolSize: 100
    schedulerAwaitTerminationSeconds: 300
    schedulerWaitForTasksToCompleteOnShutdown: true
    # app
    maxPeriodQuerySeconds: 1800 # 30'
    firstPeriodTimeSecond: 60
    firstPeriodDelayTimeMillisecond: 2000
    last29MinutestPeriodDelayMillisecond: 180000 # 3'
  cron:
    eKycUpdateAccNumJob: '0 */15 * * * ?'
    eKycUpdateAccNumJobActiveStatus: true
    initiateFptEContractJob: '0 */30 * * * ?'
    initiateFptEContractJobJobActiveStatus: true
    eKycUpdateAccNumJobTimeIntervalMilliseconds: 450000
    initiateFptEContractJobIntervalMilliseconds: 900000 # 30'
  # HDMTK
  templateEContract:
    defaultFields:
      hdmtk:
        attrs: 'null'
        # HDMTK: Custom data
        selector: 'flow_start_nhsv_create_econtract_from_template_integrate'
        payload: 'PLHD'
        recipientId: 'p_002_r_001'
        country: 'Việt Nam'
        type: 'FCA'
        photoFrontSideIDCardContentType: 'truoc.jpg'
        photoBackSideIDCardContentType: 'sau.jpg'
        statusCode: '0'
        passportID: 'null'
        resourceType: 'internal'
        refId: 'internal'
        # HDMTK: Input data
        alias: 'HDMTK'
        syncType: 'sync'
        # HDMTK: Input data -> datas
        datas:
          dfvEnvNo: 'HDMTK-20241124/001'
          dfvEnvSubmittedFrom: '/1899/5511'
          dfvP_001: 'CÔNG TY TRÁCH NHIỆM HỮU HẠN CHỨNG KHOÁN NH VIỆT NAM'
          dfvP_001_r_001_name_recipient: 'NH'
          dfvP_001_r_001_mail_recipient: 'support@nhsv.vn'
          dfvP_001_r_001_phone_recipient: 'null'
          dfvP_001_r_001_contact_recipient: '123456789'
          dfvP_002: 'individual'
          dfvP_002_r_001_applicationForm_recipient: ' '
          dfvIssueOrganization_CCSQLHCVTTXH: 'Cục cảnh sát QLHC về TTXH'
          dfvIssueOrganization_CCSDKQLCTVDLQGVDC: 'Cục Cảnh Sát ĐKDL Cư trú và DLQG về Dân cư'
          dfvNhsvRepresentative: 'Ông Kim Jong Seok'
          dfvNhsvRepresentativePotition: 'Tổng Giám đốc'
          dfvAuthorizationDocNo: ''
          dfvAuthorizationDate: ''
          dfvAccountTypeA: 'x'
          dfvAccountTypeB: ' '
          dfvAccountTypeC: ' '
          dfvDueDays: '90'
EndOfMessage
)

echo "$tmp" > $TRADEX_WORKING_DIR/application.yml


tmp=$(cat << EndOfMessage
-----BEGIN PRIVATE KEY-----
MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBANTnOFx9ALWzDKUnxdV1BK7TssNGSWKR//brmd59XFzFQk9kAodgtm8FJY/UbLXevWncL3OZ5v+g/u/+oRSO8f8hFVyClvfFtMBPVYSo9W3oopTYPvlUd0xkqxxWel6itTdUiqJ2H5UlMLek8TpZI9IPI9PWTo5d7MysDR7SRHDZAgMBAAECgYAgXncnOKoe9fX6Ni3R4lSv7+fB4LHPlrr/45olRZIBWMxYHbB0vLN/9ZxcXcOZcyABNETSopeITgX0nxYGdiU8iduJZexb9O2ea83COTKOZiLdL6CJ4lyMi6Ro8UH+DyvWTLEF61teS10vOH6OtfVoZOrMs4d0JKJiMLjeAXW78QJBAO+p1SkeiEx0s+DWk0olaw4w/CryhwuCZqrl1nf7HQfrqx8Awqedl8Y5YF+sc6wgQBZngweo3w3wfgw//+LkFa8CQQDjamrYPlJ1nN3XHG/oMiq10z7Kv9F6beK9jG0Vm/JWzsBMyDBECyUs7jsRh5jTwVNtgMUvjiiueBKG2jSPSwv3AkABrbPTfOZBetPDsF0JuOdeCxPJDTfK6dfkPIXbA0Q5yPnC1tOGLwFgHwjAwslwnC02uvTc+d7ODzAiz9Pv9977AkEAqOBOpXRqUZKCnoosg/Y1Bz7uoysciNjvBqkwHFro9BOmc545UV4hZiMm1Baoo58tr+RvCah8h0r9Hw0M+NfW8wJAQHBET7lTMpORuQy6cBkoUMUO1C5Ir4QW9HTDn6bJbmzcvxxQmPU29qop63RTccVysXnm8WSj+E3y7rkQy7j6YA==
-----END PRIVATE KEY-----
EndOfMessage
)
echo "$tmp" > $keyDir/private.pem

tmp=$(cat << EndOfMessage
-----BEGIN PUBLIC KEY-----
MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAJ03IanZNBoTThOHdXryZmSmMcWV21UdxrTLjrSF9vg6wE0jJ4DfmKMP5lePwHlEsdjDy8MM3OXDrLXtqxrgEJkCAwEAAQ==
-----END PUBLIC KEY-----
EndOfMessage
)
echo "$tmp" > $keyDirVnpt/public.pem
