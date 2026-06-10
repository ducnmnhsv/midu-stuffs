#!/usr/bin/env bash
# sleep 3
export HTS_USER0=difimarketdata
export HTS_PASSWORD0=nhsvrdf03

export HTS_USER1=difimarketdata1
export HTS_PASSWORD1=nhsvrdf03

export HTS_USER2=difimarketdata2
export HTS_PASSWORD2=nhsvrdf03

export HTS_USER3=difimarketdata3
export HTS_PASSWORD3=nhsvrdf03

export HTS_USER4=difimarketdata4
export HTS_PASSWORD4=nhsvrdf03

export HTS_USER5=difimarketdata5
export HTS_PASSWORD5=nhsvrdf03

export HTS_USER6=difimarketdata6
export HTS_PASSWORD6=nhsvrdf03

export HTS_USER7=difimarketdata7
export HTS_PASSWORD7=nhsvrdf03

export HTS_USER8=difimarketdata8
export HTS_PASSWORD8=nhsvrdf03

export HTS_USER9=difimarketdata9
export HTS_PASSWORD9=nhsvrdf03

export HTS_USER10=dfimarketdata10
export HTS_PASSWORD10=nhsvrdf03

export HTS_USER11=dfimarketdata11
export HTS_PASSWORD11=nhsvrdf03

export HTS_USER12=dfimarketdata12
export HTS_PASSWORD12=nhsvrdf03

export HTS_USER13=dfimarketdata13
export HTS_PASSWORD13=nhsvrdf03

export HTS_USER14=dfimarketdata14
export HTS_PASSWORD14=nhsvrdf03

export HTS_USER15=dfimarketdata15
export HTS_PASSWORD15=nhsvrdf03

export HTS_USER16=dfimarketdata16
export HTS_PASSWORD16=nhsvrdf03

export HTS_USER17=dfimarketdata17
export HTS_PASSWORD17=nhsvrdf03

export HTS_USER18=dfimarketdata18
export HTS_PASSWORD18=nhsvrdf03

export HTS_USER19=dfimarketdata19
export HTS_PASSWORD19=nhsvrdf03

export HTS_USER20=dfimarketdata20
export HTS_PASSWORD20=nhsvrdf03

enableInitData=true
if [[ "$TRADEX_ENV_INIT_MARKET_BY_REALTIME" != "" ]]; then
  enableInitData=false
fi


if [[ "$TRADEX_ENV_NODE_ID" == "9" && "$TRADEX_ENV_INSTANCE_ID" == "2" ]]; then
  enableInitData=true
fi


export WS_CON=""
export SRC_IP="118.70.73.${TRADEX_ENV_NODE_ID}${TRADEX_ENV_INSTANCE_ID}"
export INIT_BY_API=true
export INIT_HAS_MULTI_INSTANCE=true

if [[ "$TRADEX_ENV_NODE_ID" == "10" ]]; then
  if [[ "$TRADEX_ENV_INSTANCE_ID" == "1" ]]; then           ###### INDEX
    export SYSTEM=nhsv$TRADEX_ENV_INSTANCE_ID
    export USER_INDEX=0
    export CODE_TYPE="INDEX"
    # export EXCHANGE="exchange: HOSE"
    export EXCHANGE=""
    export NO_THREAD=1
    export NOT_SEND_OTP="true"
    export NAME=Rl-Index-$TRADEX_ENV_INSTANCE_ID
    export multipleConnectionsSplitBySorted=true
    export multipleConnections="[]"
    export TOPICS=$(cat << EndOfMessage
        topics:
          - IndexAutoItem
EndOfMessage
)
    export WS_CON=$(cat << EndOfMessage
      - name: INDEX-WS
        url: ws://172.33.30.36:9900
        codeType: INDEX
        channels: 
          - sub/pro.pub.auto.idxqt./<code> # index quote
        codeMapping:
          VNINDEX: VN
      - name: SESSION-WS
        url: ws://172.33.30.36:9900
        codeType: MARKET-STATUS
        channels:
          - sub/pro.pub.auto.tickerNews.*/ # session status
EndOfMessage
)

  else                                               #### CW
    export SYSTEM=nhsv$((TRADEX_ENV_INSTANCE_ID - 2))
    export USER_INDEX=1
    export CODE_TYPE="CW"
    # export EXCHANGE="exchange: HOSE"
    export EXCHANGE=""
    export NO_THREAD=1
    export NOT_SEND_OTP="true"
    export multipleConnectionsSplitBySorted=true
    export multipleConnections="[]"
    if [[ "$TRADEX_ENV_INSTANCE_ID" == "3" ]]; then
      export NAME=Rl-Cw
      export TOPICS=$(cat << EndOfMessage
        topics:
          - StockAutoItem
          - BidOfferAutoItem
EndOfMessage
)
      export TOPIC_MAPPING=$(cat << EndOfMessage
        topicMapping:
          StockAutoItem: CWAutoItem
          BidOfferAutoItem: CWBidOfferAutoItem
EndOfMessage
)
    else
      export NAME=Rl-Cw-Quote
      export TOPICS=$(cat << EndOfMessage
        topics:
          - StockAutoItem
EndOfMessage
)
      export TOPIC_MAPPING=$(cat << EndOfMessage
        topicMapping:
          StockAutoItem: CWAutoItem
EndOfMessage
)
    fi
    export WS_CON=$(cat << EndOfMessage
      - name: CW
        url: ws://172.33.30.36:9900
        codeType: CW
        channels:
          - sub/pro.pub.auto.qt./ # quote
          - sub/pro.pub.auto.bo./ # bid-ask
      - name: ETF
        url: ws://172.33.30.36:9900
        codeType: ETF
        channels:
          - sub/pro.pub.auto.qt./ # quote
          - sub/pro.pub.auto.bo./ # bid-ask
EndOfMessage
)

  fi
else #### STOCK - QUOTE
  if [[ "$TRADEX_ENV_INSTANCE_ID" == "1" ]]; then           #### STOCK - QUOTE - HOSE
    export SYSTEM=nhsv$TRADEX_ENV_INSTANCE_ID
    export USER_INDEX=3
    export CODE_TYPE="STOCK"
    export EXCHANGE="exchange: HOSE"
    export NO_THREAD=2
    export NOT_SEND_OTP="true"
    export NAME="Rl-Stock-Q$TRADEX_ENV_INSTANCE_ID"
    export multipleConnectionsSplitBySorted=true
    export multipleConnections=$(cat << EndOfMessage

          - name: Rl-Stock-HQ$TRADEX_ENV_INSTANCE_ID-1
            username: $HTS_USER3
            password: $HTS_PASSWORD3
          - name: Rl-Stock-HQ$TRADEX_ENV_INSTANCE_ID-2
            username: $HTS_USER4
            password: $HTS_PASSWORD4

EndOfMessage
)
    export TOPICS=$(cat << EndOfMessage
        topics:
          - StockAutoItem
          - DealNoticeAutoItem
EndOfMessage
)

    export WS_CON=$(cat << EndOfMessage
      - name: STOCK-HOSE
        url: ws://172.33.30.36:9900
        codeType: STOCK
        exchange: HOSE
        channels:
          - sub/pro.pub.auto.qt./ # quote
          - sub/pro.pub.auto.bo./ # bid-ask
EndOfMessage
)

  elif [[ "$TRADEX_ENV_INSTANCE_ID" == "2" ]]; then           #### STOCK - QUOTE - HNX
    export SYSTEM=nhsv$((TRADEX_ENV_INSTANCE_ID - 2))
    export USER_INDEX=2
    export CODE_TYPE="STOCK"
    export EXCHANGE="exchange: HNX"
    export NO_THREAD=2
    export NOT_SEND_OTP="true"
    export NAME="Rl-Stock-Q$TRADEX_ENV_INSTANCE_ID"
    export multipleConnectionsSplitBySorted=true
    export multipleConnections=$(cat << EndOfMessage

          - name: Rl-Stock-NQ$TRADEX_ENV_INSTANCE_ID-1
            username: $HTS_USER5
            password: $HTS_PASSWORD5
          - name: Rl-Stock-NQ$TRADEX_ENV_INSTANCE_ID-2
            username: $HTS_USER6
            password: $HTS_PASSWORD6

EndOfMessage
)
    export TOPICS=$(cat << EndOfMessage
        topics:
          - StockAutoItem
          - DealNoticeAutoItem
EndOfMessage
)
    export WS_CON=$(cat << EndOfMessage
      - name: STOCK-HNX
        url: ws://172.33.30.36:9900
        codeType: STOCK
        exchange: HNX
        channels:
          - sub/pro.pub.auto.qt./ # quote
          - sub/pro.pub.auto.bo./ # bid-ask
EndOfMessage
)
  elif [[ "$TRADEX_ENV_INSTANCE_ID" == "3" ]]; then           #### STOCK - QUOTE - UPCOM
    export SYSTEM=nhsv$((TRADEX_ENV_INSTANCE_ID - 4))
    export USER_INDEX=3
    export CODE_TYPE="STOCK"
    export EXCHANGE="exchange: UPCOM"
    export NO_THREAD=2
    export NOT_SEND_OTP="true"
    export NAME="Rl-Stock-Q$TRADEX_ENV_INSTANCE_ID"
    export multipleConnectionsSplitBySorted=true
    export multipleConnections=$(cat << EndOfMessage

          - name: Rl-Stock-NQ$TRADEX_ENV_INSTANCE_ID-1
            username: $HTS_USER7
            password: $HTS_PASSWORD7
          - name: Rl-Stock-NQ$TRADEX_ENV_INSTANCE_ID-2
            username: $HTS_USER8
            password: $HTS_PASSWORD8

EndOfMessage
)
    export TOPICS=$(cat << EndOfMessage
        topics:
          - StockAutoItem
          - DealNoticeAutoItem
EndOfMessage
)
    export WS_CON=$(cat << EndOfMessage
      - name: STOCK-UPCOM
        url: ws://172.33.30.36:9900
        codeType: STOCK
        exchange: UPCOM
        channels:
          - sub/pro.pub.auto.qt./ # quote
          - sub/pro.pub.auto.bo./ # bid-ask
EndOfMessage
)

  fi
fi

export USER="HTS_USER${USER_INDEX}"
export PASSWORD="HTS_PASSWORD${USER_INDEX}"

hts_accounts=$(cat << EndOfMessage
    accounts:
      - system: $SYSTEM
        username: "${!USER}"
        password: "${!PASSWORD}"
        notSendOtp: $NOT_SEND_OTP
        mediaType: \${app.mediaType}
        name: $NAME
        codeType: $CODE_TYPE
        $EXCHANGE
        noOfThread: $NO_THREAD
        ip: "$SRC_IP"
        resubscribeAfterMs: 600000
        multipleConnectionsSplitBySorted: $multipleConnectionsSplitBySorted
        multipleConnections: $multipleConnections
$TOPICS
$TOPIC_MAPPING

EndOfMessage
)

if [[ "$TRADEX_ENV_NODE_ID" == "9" ]]; then
  if [[ "$TRADEX_ENV_INSTANCE_ID" == "2" ]]; then
hts_accounts=$(cat << EndOfMessage
    accounts: []
EndOfMessage
)
  fi
fi

export special_hts_account="    accounts: []"
tmp=$(cat << EndOfMessage
spring:
  application:
    name: "market-collector-lotte"
  data:
    mongodb:
      uri: "mongodb://${TRADEX_ENV_MONGO_TRADEX_MARKET_USER}:${TRADEX_ENV_MONGO_TRADEX_MARKET_PASSWORD}@${TRADEX_ENV_MONGO_HOST}:${TRADEX_ENV_MONGO_PORT}/tradex-market"
    redis:
      host: $TRADEX_ENV_REDIS_HOST
server:
  port: 59999
app:
  key: eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbnYiOiJwcm9kIiwiaWF0IjoxNzI1NTkzMTMyLCJleHAiOjE3NjcyMzc5MzIsImlzcyI6IkRpZmlzb2Z0Iiwic3ViIjoibWFya2V0LWNvbGxlY3Rvci1sb3R0ZSJ9.GvYdWu3eyJoCW5JQqkopJTZAQw5hiuGd-oGfMlqYvuwc1AM268mRWVdxvnNJvqQmaHDZfnAx0ZWTmYgb4KOuh1RE9pksoR9B7gebHO8505byfbZbFnUgZV9BWgPNjSbCyYk0qrFj8R_uouzXyeO2DZhds__Gft--1cBqutaKsMb-LrbER-z6h39N5TmzNEH8uUe8qelh_9Ojp8Wu0AJRYF3siCnuKpQ2VL-QO1vRLPRVjcMafojVp1UecUHRvbAfFQXkju-S3c1VwT8_LshlXPbTKlJYFBRPhRA60rc3wkoUq1v90Ly8YcLnS7gd7HlM2w-lz_4jLcz5LgWY7-FLxg
  enableIgnoreQuote: false
  enableQuery: false
  enableInitMarket: $enableInitData
  enableStoreConnectionInfo: false
  enableMultipleInstance: $INIT_HAS_MULTI_INSTANCE
  kafkaUrl: $TRADEX_ENV_KAFKA_PRIMARY_URL
  timeStartReceiveBidAsk: "01:40"
  timeStopReceiveBidAsk: "23:00"
  isUsingApi: $INIT_BY_API
  apiConnection: 
    baseUrl: "http://172.33.30.36:8100/"
    apiKey: "qJxAtDmMuDEan9kUWsY4UuMugFvrYeMe"
  marketConf:
    symbolStaticBucket: market
    fileConfig:
      defaultType: MINIO
      minio:
        baseUrl: http://$MINIO_HOST:9000
        accessKey: "$MINIO_ACCESS_KEY"
        privateKey: "$MINIO_SECRET_KEY"
        buckets:
          - name: market
  accountDownload:
    system: nhsv1
    username: "${HTS_USER20}"
    password: "${HTS_PASSWORD20}"
    notSendOtp: true
    mediaType: "04"
  realtime:
    websocketConnections: 
$WS_CON
#    accounts: []
#  --hts_accounts
$special_hts_account

  sysConf:
    nhsv1:
      host: 172.33.30.12
      loginPort: 8000
      dataPort: 21001
      marketPort: 22001
      secCode: "039"
    nhsv2:
      host: 172.33.30.11
      loginPort: 8000
      dataPort: 21001
      marketPort: 22001
      secCode: "039"
  schedulers:
    resetCacheMapLastTradingVolume: "0 0 0,1,11,12,13,14,15,16 * * MON-FRI"
    resetCacheMapSequence: "0 0 0,1,11,12,13,14,15,16 * * MON-FRI"
    startRealtime1st: "0 00 2 * * MON-FRI"
    stopRealtime1st: "0 05 8 * * MON-FRI"
    startRealtime2nd: "0 06 8 * * MON-FRI"
    stopRealtime2nd: "0 07 8 * * MON-FRI"
    downloadSymbol: "0 5,15,30,40,50 1 * * MON-FRI"
    saveMonitorData: "0 0 5,9 * * *"
    resetMonitorData: "0 0 17 * * *"
    riseFallStockRankSave: "0 */1 1-18 * * *"
    rightInfoSave: "0 40 */1 * * *"
  holidays:
    - 20240208
    - 20240209
    - 20240210
    - 20240211
    - 20240212
    - 20240213
    - 20240214
  holidayUrl: "http://$MINIO_HOST:9000/mts/holidays.json"

logging:
  file:
    name: /logs/application.log
  level:
    ROOT: INFO
    org.hibernate.SQL: INFO
    com.difisoft.marketcollector: INFO
    org.springframework: INFO
    com.difisoft.htsconnection.socket.nonblocking: INFO

EndOfMessage
)

echo "$tmp" > $TRADEX_WORKING_DIR/application.yml