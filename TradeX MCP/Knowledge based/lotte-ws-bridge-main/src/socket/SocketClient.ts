import { Logger } from 'tradex-common';
import {
  sendOneSignalAdvancePaymentSecuritiesSales,
  sendOneSignalAnnouncementBuyOrderMatching,
  sendOneSignalAnnouncementSellOrderMatching,
  sendOneSignalCancelBuyIssueStock,
  sendOneSignalChangePassword,
  sendOneSignalDepositStock,
  sendOneSignalDividendPayment,
  sendOneSignalMarginDisbursement,
  sendOneSignalNoticeReceiveBonusStock,
  sendOneSignalReceiveStockTransfer,
  sendOneSignalRegisterBuyIssueStock,
  sendOneSignalRightToBuyStockIssue,
  sendOneSignalSendMoney,
  sendOneSignalStockDividendsPayment,
  sendOneSignalWithdrawMoney,
} from '../service/SendNotificationService';
import { ILOTTEHPTWsMessage } from '../model/request/ILOTTEHPTWsMessage';
import conf from '../config';
import { IChangePasswordRequest } from '../model/request/IChangePasswordRequest';
import { IDividendPaymentRequest } from '../model/request/IDividendPaymentRequest';
import { IReceiveStockTransferRequest } from '../model/request/IReceiveStockTransferRequest';
import { IRightToBuyStockIssueRequest } from '../model/request/IRightToBuyStockIssueRequest';
import { INoticeReceiveBonusStockRequest } from '../model/request/INoticeReceiveBonusStockRequest';
import { IStockDividendsPaymentRequest } from '../model/request/IStockDividendsPaymentRequest';
import { IAnnouncementBuyOrderMatchingRequest } from '../model/request/IAnnouncementBuyOrderMatchingRequest';
import { IAnnouncementSellOrderMatchingRequest } from '../model/request/IAnnouncementSellOrderMatchingRequest';
import { IRegisterBuyIssueStockRequest } from '../model/request/IRegisterBuyIssueStockRequest';
import { ICancelBuyIssueStockRequest } from '../model/request/ICancelBuyIssueStockRequest';
import { IAdvancePaymentSecuritiesSalesRequest } from '../model/request/IAdvancePaymentSecuritiesSalesRequest';
import { ISendMoneyRequest } from '../model/request/ISendMoneyRequest';
import { IWithdrawMoneyRequest } from '../model/request/IWithdrawMoneyRequest';
import { IMarginDisbursementRequest } from '../model/request/IMarginDisbursementRequest';

const WebSocket = require('ws');

class SocketClient {
  private socket: WebSocket;
  private orgName: string;
  private connectIndex: number = 0;
  private name: string;
  private pong: boolean | null = null;
  private lastReceivedData: number = 0;

  constructor(name: string) {
    this.orgName = name;
  }

  private setConnectIndex() {
    this.connectIndex++;
    if (this.connectIndex >= 1000000) {
      this.connectIndex = 1;
    }
    this.name = `${this.orgName}-${this.connectIndex}`;
  }

  checkReceivedData = () => {
    if (new Date().getTime() - this.lastReceivedData > conf.checkReceivedCycleMs) {
      Logger.info(this.name, 'Not received data for a while. reconnecting...');
      this.reconnect();
    }
  };

  reconnect = () => {
    try {
      this.socket.close();
      // it will reconnect when the close event is fired
    } catch (e) {
      Logger.error(e);
    }
  };

  checkPingPong = () => {
    if (this.socket != null && this.socket.readyState === this.socket.OPEN) {
      if (this.pong === false) {
        Logger.info(this.name, 'Not received pong from server, reconnecting...');
        this.reconnect();
      } else {
        this.pong = false;
        Logger.info(this.name, 'Ping server');
        this.socket.send('req/echo/ping');
      }
    }
  };

  private onMessage = (event) => {
    const message = event.data;
    this.pong = true;
    let data: ILOTTEHPTWsMessage | null = null;
    try {
      data = JSON.parse(message);
    } catch (e) {
      Logger.info(this.name, `Received message non json: ${message}`);
      return;
    }
    Logger.info(this.name, `Received message :${message}`);
    this.lastReceivedData = new Date().getTime();
    try {
      const messageLOTTEHPT: ILOTTEHPTWsMessage = data!;
      handleMessage(messageLOTTEHPT);
    } catch (e) {
      Logger.error(this.name, 'error on handle message', e);
    }
  };

  subscribe = () => {
    if (this.socket != null && this.socket.readyState === this.socket.OPEN) {
      this.socket.send('sub/bos.evt.ord.sts.*/');
      this.socket.send('sub/bos.evt.acc.bal.*/');
    }
  };

  onOpen = () => {
    Logger.info(this.name, 'WebSocket connected successfully');
    this.subscribe();
  };

  connect = () => {
    this.setConnectIndex();
    const socket = new WebSocket(conf.lotte.websocketAddress);
    this.socket = socket;

    socket.onopen = this.onOpen;

    socket.onmessage = this.onMessage;

    socket.onclose = (): void => {
      Logger.info(this.name, 'WebSocket disconnected');
      setTimeout(this.connect, 1000);
    };

    socket.onerror = (event) => {
      Logger.error(this.name, 'socket error', event);
    };

    socket.on('pong', () => {
      Logger.info(this.name, 'Has received ws pong from server');
    });

    socket.on('ping', () => {
      Logger.info(this.name, 'Has received ws ping from server');
    });

    socket.on('error', (error) => {
      Logger.error(this.name, 'Ws Error', error);
    });

    socket.on('upgrade', () => {
      Logger.warn(this.name, 'upgrade');
    });

    socket.on('unexpected-response', (request, response) => {
      Logger.warn(this.name, 'unexpected-response', request, response);
    });

    socket.on('redirect', (url, request) => {
      Logger.warn(this.name, 'redirect', url, request);
    });
  };
}

interface ISeqNo {
  event_seqno: string;
}

function checkDuplicate<T extends ISeqNo>(request: T, handler: (data: T) => void, map: Map<string, boolean>) {
  if (!map.has(request.event_seqno)) {
    const err: unknown | null = null;
    try {
      handler(request);
    } catch (e) {
      e = err;
    }

    map.set(request.event_seqno, true);
    if (map.size > conf.duplicateCheckSize) {
      for (const k of map.keys()) {
        map.delete(k);
        break;
      }
    }
    if (err != null) {
      throw err;
    }
  }
}

const mapF01104: Map<string, boolean> = new Map();
const mapF02104: Map<string, boolean> = new Map();
const mapF02105: Map<string, boolean> = new Map();
const mapF02107: Map<string, boolean> = new Map();
const mapF02111: Map<string, boolean> = new Map();
const mapF02115: Map<string, boolean> = new Map();
const mapF02116: Map<string, boolean> = new Map();
const mapF03101: Map<string, boolean> = new Map();
const mapF03102: Map<string, boolean> = new Map();
const mapF03108: Map<string, boolean> = new Map();
const mapF03110: Map<string, boolean> = new Map();
const mapF04101: Map<string, boolean> = new Map();
const mapF07101: Map<string, boolean> = new Map();
const mapF07102: Map<string, boolean> = new Map();
const mapF04103: Map<string, boolean> = new Map();

async function handleMessage(message: ILOTTEHPTWsMessage): Promise<void> {
  if (message && message.eventCode) {
    switch (message.eventCode) {
      case 'F01104':
        const rF01104: IChangePasswordRequest = JSON.parse(message.eventBody);
        checkDuplicate(rF01104, sendOneSignalChangePassword, mapF01104);
        break;
      case 'F02104':
        const rF02104: IDividendPaymentRequest = JSON.parse(message.eventBody);
        checkDuplicate(rF02104, sendOneSignalDividendPayment, mapF02104);
        break;
      case 'F02105':
        const rF02105: IReceiveStockTransferRequest = JSON.parse(message.eventBody);
        checkDuplicate(rF02105, sendOneSignalReceiveStockTransfer, mapF02105);
        break;
      case 'F02107':
        const rF02107: IRightToBuyStockIssueRequest = JSON.parse(message.eventBody);
        checkDuplicate(rF02107, sendOneSignalRightToBuyStockIssue, mapF02107);
        break;
      case 'F02111':
        const rF02111: INoticeReceiveBonusStockRequest = JSON.parse(message.eventBody);
        checkDuplicate(rF02111, sendOneSignalNoticeReceiveBonusStock, mapF02111);
        break;
      case 'F02115':
        const rF02115: INoticeReceiveBonusStockRequest = JSON.parse(message.eventBody);
        checkDuplicate(rF02115, sendOneSignalDepositStock, mapF02115);
        break;
      case 'F02116':
        const rF02116: IStockDividendsPaymentRequest = JSON.parse(message.eventBody);
        checkDuplicate(rF02116, sendOneSignalStockDividendsPayment, mapF02116);
        break;
      case 'F03101':
        const rF03101: IAnnouncementBuyOrderMatchingRequest = JSON.parse(message.eventBody);
        checkDuplicate(rF03101, sendOneSignalAnnouncementBuyOrderMatching, mapF03101);
        break;
      case 'F03102':
        const rF03102: IAnnouncementSellOrderMatchingRequest = JSON.parse(message.eventBody);
        checkDuplicate(rF03102, sendOneSignalAnnouncementSellOrderMatching, mapF03102);
        break;
      case 'F03108':
        const rF03108: IRegisterBuyIssueStockRequest = JSON.parse(message.eventBody);
        checkDuplicate(rF03108, sendOneSignalRegisterBuyIssueStock, mapF03108);
        break;
      case 'F03110':
        const rF03110: ICancelBuyIssueStockRequest = JSON.parse(message.eventBody);
        checkDuplicate(rF03110, sendOneSignalCancelBuyIssueStock, mapF03110);
        break;
      case 'F04101':
        const rF04101: IAdvancePaymentSecuritiesSalesRequest = JSON.parse(message.eventBody);
        checkDuplicate(rF04101, sendOneSignalAdvancePaymentSecuritiesSales, mapF04101);
        break;
      case 'F07101':
        const rF07101: ISendMoneyRequest = JSON.parse(message.eventBody);
        checkDuplicate(rF07101, sendOneSignalSendMoney, mapF07101);
        break;
      case 'F07102':
        const rF07102: IWithdrawMoneyRequest = JSON.parse(message.eventBody);
        checkDuplicate(rF07102, sendOneSignalWithdrawMoney, mapF07102);
        break;
      case 'F04103':
        const rF04103: IMarginDisbursementRequest = JSON.parse(message.eventBody);
        checkDuplicate(rF04103, sendOneSignalMarginDisbursement, mapF04103);
        break;
      default:
        return;
    }
  }
}

export function connectSocket() {
  const socket1 = new SocketClient('main');
  const socket2 = new SocketClient('backup');
  socket1.connect();
  socket2.connect();

  let intervalIndex = 0;
  setInterval(() => {
    intervalIndex++;
    if (intervalIndex >= 1000000000) {
      intervalIndex = 0;
    }
    if (intervalIndex % conf.pingCycle === 0) {
      socket1.checkPingPong();
      socket2.checkPingPong();
    }
    if (intervalIndex % conf.checkReceivedCycleDouble === 0) {
      socket1.checkReceivedData();
    }
    if (intervalIndex % conf.checkReceivedCycleDouble === conf.checkReceivedCycle) {
      socket2.checkReceivedData();
    }
    if (intervalIndex % conf.resubscribeCycle === 0) {
      socket1.subscribe();
      socket2.subscribe();
    }
  }, conf.checkInterval);
}
