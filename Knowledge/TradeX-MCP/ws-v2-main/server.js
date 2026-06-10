const conf = require('./conf');
const http = require('http');
const socketClusterServer = require('socketcluster-server');
const scCodecMinBin = require('sc-codec-min-bin');
const express = require('express');
const healthChecker = require('sc-framework-health-check');
const serveStatic = require('serve-static');
const path = require('path');
const TradexCommon = require('tradex-common');
const channelPublishHandler = require('./channelPublishHandler');
const channelSubscribeHandler = require('./channelSubscribeHandler');
const { scAuthenHandler } = require('./sccClusterHandler');


const {
  mapTopicToPublishV2,
  mapTopicToPublish,
  communicationTypes,
} = require('./constants');

const {
  loadServiceConfig,
} = require('./loadServiceConfig');

const {
  loginMap,
} = require('./cache');

const {
  createMarketProcess
} = require('./market');

const { doQuery } = require('./query');


const { 
  verifyOtp,
  login,
  onlogOut,
 } = require('./authen');

const { loadScopes } = require('./scope');
const { requestHandler } = require('./requestHandler');

const logger = TradexCommon.Logger;
let verifyConf = null;
if (conf.allowOnly1SessionPerUser) {
  const verifyType = conf.allowOnly1SessionPerUserConf.verifyType;
  verifyConf = conf.allowOnly1SessionPerUserConf.verifyApis[verifyType];
  logger.info("verify authen state", verifyConf);
}

class ServerTradexWs {
  constructor(options) {
    logger.info(`ServerTradexWs init with options:`, options);
    this.options = options;

    this.onConnect = this.onConnect.bind(this);
    this.severReady = this.severReady.bind(this);
    this.start = this.start.bind(this);
    this.requestHandler = this.requestHandler.bind(this);
    this.oneSessionPerPerson = this.oneSessionPerPerson.bind(this);
    this.communicationHandler = this.communicationHandler.bind(this);
    this.wsBroadcastHandler = this.wsBroadcastHandler.bind(this);
    this.orderMatchHandler = this.orderMatchHandler.bind(this);
    this.updateConditionalOrderHandler = this.updateConditionalOrderHandler.bind(this);
  }

  start() {
    this.httpServer = http.createServer();
    this.scServer = socketClusterServer.attach(this.httpServer, this.options);
    console.log(`server listen port: ${this.options.port}`);
    logger.info(`server listen port: ${this.options.port}`);
    this.httpServer.listen(this.options.port, () => this.severReady());
    //init
    //setCodeEngine
    this.scServer.setCodecEngine(scCodecMinBin);
    //using public httpserver
    const app = express();
    app.use(serveStatic(path.resolve(__dirname, 'public')));
    // Add GET /health-check express route
    healthChecker.attach(this, app);
    this.httpServer.on('request', app);
    //handle connection
    this.scServer.on('connection', this.onConnect);
    TradexCommon.Kafka.create(
      conf,
      conf.kafkaConsumerOptions,
      true,
      { 'auto.offset.reset': 'earliest' },
      conf.kafkaProducerOptions,
      () => {
        logger.info('kafka instance is ready');
        loadScopes(this.isLeader, () => logger.info("finish loading scope"))
      }
    );
    loadScopes(true);

    /** Init i18next */
    TradexCommon.Utils.initI18nInternal('ws', conf.i18nNamespaceList);

    this.kafkaInstance = TradexCommon.Kafka.getInstance();
  }



  severReady() {
    logger.info(`severReady`);
    /**
     * request handler
     * @type {MessageHandler}
     */
    const messageHandler = new TradexCommon.Kafka.MessageHandler();
    new TradexCommon.Kafka.StreamHandler(
      conf,
      conf.kafkaConsumerOptions,
      [conf.clusterId],
      message => messageHandler.handle(message, this.requestHandler),
      conf.kafkaTopicOptions
    );

    // if allowOnly1SessionPerUser is enable. we will listen login notification to disconnect old session
    this.oneSessionPerPerson();

    const processMarketFunction = createMarketProcess(conf.isHandlerKafkaV2 === true, this.scServer.exchange);
    if (conf.isHandlerKafkaV2 === true) {
      logger.info("consuming market data v2");
      new TradexCommon.Kafka.StreamHandler(conf, { 'group.id': conf.clientId + '.v2' }, Object.keys(mapTopicToPublishV2), processMarketFunction, conf.kafkaTopicOptions);
    } else {
      logger.info("consuming market data v1");
      new TradexCommon.Kafka.StreamHandler(conf, { 'group.id': conf.clientId + '.v1' }, Object.keys(mapTopicToPublish), processMarketFunction, conf.kafkaTopicOptions);
    }

    logger.info("consuming publish data topic");
    new TradexCommon.Kafka.StreamHandler(conf, { 'group.id': conf.clientId }, ['ws.broadcast'], this.wsBroadcastHandler, conf.kafkaTopicOptions);

    logger.info("consuming order match topic");
    new TradexCommon.Kafka.StreamHandler(conf, { 'group.id': conf.clientId }, ['orderMatch'], this.orderMatchHandler, conf.kafkaTopicOptions);

    logger.info("consuming topic update conditional order");
    new TradexCommon.Kafka.StreamHandler(conf, { 'group.id': conf.clientId }, ['updateConditionalOrder'], this.updateConditionalOrderHandler, conf.kafkaTopicOptions);
    this.scServer.addMiddleware(this.scServer.MIDDLEWARE_SUBSCRIBE, (req, next) => channelSubscribeHandler(req, next));
    this.scServer.addMiddleware(this.scServer.MIDDLEWARE_AUTHENTICATE, scAuthenHandler);
    this.scServer.addMiddleware(this.scServer.MIDDLEWARE_PUBLISH_IN, channelPublishHandler);
  }

  onConnect(socket) {
    logger.info(`_connection socketId:`, socket.id);
    socket.on('doQuery', (data, res) => doQuery(socket, data, res).then().catch(err => logger.error(socket.id, "doQuery:", err)));
    socket.on('loadServiceConfig', (data, res) => loadServiceConfig(socket, data, res));
    socket.on('logout', (data, respond) => onlogOut(socket, data, respond));
    socket.on('login/sec/verifyOTP', (request, respond) => verifyOtp(socket, request, respond).then().catch(err => logger.error(socket.id, "verifyOtp:", err)));
    socket.on('login', (request, respond) => login(socket, request, respond).then().catch(err => logger.error(socket.id, "login:", err)));
    socket.on('systemLogin', (request, respond) => systemLogin(socket, request, respond));
    socket.on('/api/v2/market/symbolInfo', (data, res) => requestHandler('/api/v2/market/symbolInfo', socket, data, res));
  }

  requestHandler(message) {
    if (message.uri === '/api/v1/publish') {
      if (this.scServer) {
        this.scServer.exchange.publish(message.data.topic, message.data.data);
      }
      return true;
    }
    return false;
  }

  communicationHandler(msg) {
    const msgStr = msg.value.toString();
    logger.info(`communication msg ${msgStr}`);
    try {
      const message = JSON.parse(msgStr);
      if (message.uri === communicationTypes.userLogin) {
        const userId = message.data.userId;
        const socketId = message.data.socketId;
        for (let key in this.scServer.clients) {
          const socket = this.scServer.clients[key];
          try {
            const currentUserId = loginMap[key];
            const currentSocketId = key;
            if (currentUserId === userId && currentSocketId !== socketId) {
              logger.info(currentSocketId, currentUserId, "de-authenticate socket");
              socket.deauthenticate();
              delete loginMap[key];
            }
          } catch (e) {
            logger.error(`error on checking check user login ${key}`.e);
          }
        }
      }
    } catch (e) {
      logger.error(`error on communication msg "${msgStr}"`, e);
    }
  }

  oneSessionPerPerson() {
    if (conf.allowOnly1SessionPerUser) {
      /**
       * communication handler
       * @type {SendRequest}
       */
      const streamConf = {
        clusterId: conf.clientId,
        kafkaUrls: conf.kafkaUrls,
        clientId: "",
      };
      logger.warn("init communication consuming");
      new TradexCommon.Kafka.StreamHandler(streamConf, conf.kafkaConsumerOptions, [conf.communicationTopic], this.communicationHandler, conf.kafkaTopicOptions);
    }
  }

  wsBroadcastHandler(msg) {
    try {
      let message = JSON.parse(msg.value.toString());
      let data = message.data;
      let channel = data.cn;
      let bd = data.bd;
      logger.info(`publish to ${channel}, with body data:`, bd);
      this.scServer.exchange.publish(channel, bd);
    } catch (e) {
      logger.error(`error on publish msg "${msg}"`, e);
    }
  }

  orderMatchHandler(msg) {
    try {
      let message = JSON.parse(msg.value.toString());
      let data = message.data;
      let channel = `orderMatch.${data.username}`;
      logger.info(`publish to ${channel}, with body data:`, data);
      this.scServer.exchange.publish(channel, data);
    } catch (e) {
      logger.error(`error on publish msg "${msg}"`, e);
    }
  }

  updateConditionalOrderHandler(msg) {
    try {
      let message = JSON.parse(msg.value.toString());
      let data = message.data;
      let channel = `updateConditionalOrder.${data.username}`;
      logger.info(`publish to ${channel}, with body data:`, data);
      this.scServer.exchange.publish(channel, data);
    } catch (e) {
      logger.error(`error on publish msg "${msg}"`, e);
    }
  }
}


module.exports = ServerTradexWs;