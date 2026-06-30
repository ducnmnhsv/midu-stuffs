import { Logger } from 'tradex-common';
import {
  sendOneSignalAdvancePaymentSecuritiesSales,
  sendOneSignalAnnouncementBuyOrderMatching,
  sendOneSignalAnnouncementSellOrderMatching,
  sendOneSignalCancelBuyIssueStock,
  sendOneSignalChangePassword,
  sendOneSignalDepositStock,
  sendOneSignalDerivativesMarginCallLevel2,
  sendOneSignalDerivativesMarginCallLevel3,
  sendOneSignalDerivativesMarginDeposit,
  sendOneSignalDerivativesMarginWithdrawal,
  sendOneSignalDerivativesOrderMatching,
  sendOneSignalDerivativesPositionWarningLevel1,
  sendOneSignalDerivativesPositionWarningLevel2,
  sendOneSignalDerivativesVmLossPayment,
  sendOneSignalDerivativesVmProfitPayment,
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
import { sendDataOrderStatusDerivative } from '../service/OrderService';
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
import { IDerivativesPositionWarningLevel1Request } from '../model/request/IDerivativesPositionWarningLevel1Request';
import { IDerivativesPositionWarningLevel2Request } from '../model/request/IDerivativesPositionWarningLevel2Request';
import { IDerivativesOrderMatchingRequest } from '../model/request/IDerivativesOrderMatchingRequest';
import { IDerivativesVmProfitPaymentRequest } from '../model/request/IDerivativesVmProfitPaymentRequest';
import { IDerivativesVmLossPaymentRequest } from '../model/request/IDerivativesVmLossPaymentRequest';
import { IDerivativesMarginCallLevel2Request } from '../model/request/IDerivativesMarginCallLevel2Request';
import { IDerivativesMarginCallLevel3Request } from '../model/request/IDerivativesMarginCallLevel3Request';
import { IDerivativesMarginDepositRequest } from '../model/request/IDerivativesMarginDepositRequest';
import { IDerivativesMarginWithdrawalRequest } from '../model/request/IDerivativesMarginWithdrawalRequest';

import { IOrderStatusDerivativeRequest } from '../model/request/IOrderStatusDerivativeRequest';
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
      this.socket.send('sub/bos.evt.acc.inf.*/');
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
  if (map.has(request.event_seqno)) {
    return;
  }
  handler(request);
  map.set(request.event_seqno, true);
  if (map.size > conf.duplicateCheckSize) {
    for (const k of map.keys()) {
      map.delete(k);
      break;
    }
  }
}

function parseEventBody<T>(eventCode: string, eventBody: string): T | null {
  const trimmed = eventBody == null ? '' : eventBody.trim();
  if (trimmed === '') {
    Logger.warn('Empty eventBody, skip event', eventCode);
    return null;
  }
  try {
    return JSON.parse(trimmed) as T;
  } catch (e) {
    Logger.error('Failed to parse eventBody', eventCode, eventBody, e);
    return null;
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
const mapF15201: Map<string, boolean> = new Map();
const mapF15202: Map<string, boolean> = new Map();
const mapF15302: Map<string, boolean> = new Map();
const mapF15401: Map<string, boolean> = new Map();
const mapF15402: Map<string, boolean> = new Map();
const mapF15403: Map<string, boolean> = new Map();
const mapF15404: Map<string, boolean> = new Map();
const mapF15701: Map<string, boolean> = new Map();
const mapF15702: Map<string, boolean> = new Map();

function handleMessage(message: ILOTTEHPTWsMessage): void {
const mapF15303: Map<string, boolean> = new Map();
async function handleMessage(message: ILOTTEHPTWsMessage): Promise<void> {
  if (message && message.eventCode) {
    switch (message.eventCode) {
      case 'F01104':
        const rF01104 = parseEventBody<IChangePasswordRequest>(message.eventCode, message.eventBody);
        if (rF01104 == null) {
          return;
        }
        checkDuplicate(rF01104, sendOneSignalChangePassword, mapF01104);
        break;
      case 'F02104':
        const rF02104 = parseEventBody<IDividendPaymentRequest>(message.eventCode, message.eventBody);
        if (rF02104 == null) {
          return;
        }
        checkDuplicate(rF02104, sendOneSignalDividendPayment, mapF02104);
        break;
      case 'F02105':
        const rF02105 = parseEventBody<IReceiveStockTransferRequest>(message.eventCode, message.eventBody);
        if (rF02105 == null) {
          return;
        }
        checkDuplicate(rF02105, sendOneSignalReceiveStockTransfer, mapF02105);
        break;
      case 'F02107':
        const rF02107 = parseEventBody<IRightToBuyStockIssueRequest>(message.eventCode, message.eventBody);
        if (rF02107 == null) {
          return;
        }
        checkDuplicate(rF02107, sendOneSignalRightToBuyStockIssue, mapF02107);
        break;
      case 'F02111':
        const rF02111 = parseEventBody<INoticeReceiveBonusStockRequest>(message.eventCode, message.eventBody);
        if (rF02111 == null) {
          return;
        }
        checkDuplicate(rF02111, sendOneSignalNoticeReceiveBonusStock, mapF02111);
        break;
      case 'F02115':
        const rF02115 = parseEventBody<INoticeReceiveBonusStockRequest>(message.eventCode, message.eventBody);
        if (rF02115 == null) {
          return;
        }
        checkDuplicate(rF02115, sendOneSignalDepositStock, mapF02115);
        break;
      case 'F02116':
        const rF02116 = parseEventBody<IStockDividendsPaymentRequest>(message.eventCode, message.eventBody);
        if (rF02116 == null) {
          return;
        }
        checkDuplicate(rF02116, sendOneSignalStockDividendsPayment, mapF02116);
        break;
      case 'F03101':
        const rF03101 = parseEventBody<IAnnouncementBuyOrderMatchingRequest>(message.eventCode, message.eventBody);
        if (rF03101 == null) {
          return;
        }
        checkDuplicate(rF03101, sendOneSignalAnnouncementBuyOrderMatching, mapF03101);
        break;
      case 'F03102':
        const rF03102 = parseEventBody<IAnnouncementSellOrderMatchingRequest>(message.eventCode, message.eventBody);
        if (rF03102 == null) {
          return;
        }
        checkDuplicate(rF03102, sendOneSignalAnnouncementSellOrderMatching, mapF03102);
        break;
      case 'F03108':
        const rF03108 = parseEventBody<IRegisterBuyIssueStockRequest>(message.eventCode, message.eventBody);
        if (rF03108 == null) {
          return;
        }
        checkDuplicate(rF03108, sendOneSignalRegisterBuyIssueStock, mapF03108);
        break;
      case 'F03110':
        const rF03110 = parseEventBody<ICancelBuyIssueStockRequest>(message.eventCode, message.eventBody);
        if (rF03110 == null) {
          return;
        }
        checkDuplicate(rF03110, sendOneSignalCancelBuyIssueStock, mapF03110);
        break;
      case 'F04101':
        const rF04101 = parseEventBody<IAdvancePaymentSecuritiesSalesRequest>(message.eventCode, message.eventBody);
        if (rF04101 == null) {
          return;
        }
        checkDuplicate(rF04101, sendOneSignalAdvancePaymentSecuritiesSales, mapF04101);
        break;
      case 'F07101':
        const rF07101 = parseEventBody<ISendMoneyRequest>(message.eventCode, message.eventBody);
        if (rF07101 == null) {
          return;
        }
        checkDuplicate(rF07101, sendOneSignalSendMoney, mapF07101);
        break;
      case 'F07102':
        const rF07102 = parseEventBody<IWithdrawMoneyRequest>(message.eventCode, message.eventBody);
        if (rF07102 == null) {
          return;
        }
        checkDuplicate(rF07102, sendOneSignalWithdrawMoney, mapF07102);
        break;
      case 'F04103':
        const rF04103 = parseEventBody<IMarginDisbursementRequest>(message.eventCode, message.eventBody);
        if (rF04103 == null) {
          return;
        }
        checkDuplicate(rF04103, sendOneSignalMarginDisbursement, mapF04103);
        break;
      case 'F15102':
      case 'F15405':
        Logger.warn('Pending derivatives event without confirmed payload sample, skip', message.eventCode);
        break;
      case 'F15201':
        const rF15201 = parseEventBody<IDerivativesPositionWarningLevel1Request>(message.eventCode, message.eventBody);
        if (rF15201 == null) {
          return;
        }
        checkDuplicate(rF15201, sendOneSignalDerivativesPositionWarningLevel1, mapF15201);
        break;
      case 'F15202':
        const rF15202 = parseEventBody<IDerivativesPositionWarningLevel2Request>(message.eventCode, message.eventBody);
        if (rF15202 == null) {
          return;
        }
        checkDuplicate(rF15202, sendOneSignalDerivativesPositionWarningLevel2, mapF15202);
        break;
      case 'F15302':
        const rF15302 = parseEventBody<IDerivativesOrderMatchingRequest>(message.eventCode, message.eventBody);
        if (rF15302 == null) {
          return;
        }
        checkDuplicate(rF15302, sendOneSignalDerivativesOrderMatching, mapF15302);
        break;
      case 'F15401':
        const rF15401 = parseEventBody<IDerivativesVmProfitPaymentRequest>(message.eventCode, message.eventBody);
        if (rF15401 == null) {
          return;
        }
        checkDuplicate(rF15401, sendOneSignalDerivativesVmProfitPayment, mapF15401);
        break;
      case 'F15402':
        const rF15402 = parseEventBody<IDerivativesVmLossPaymentRequest>(message.eventCode, message.eventBody);
        if (rF15402 == null) {
          return;
        }
        checkDuplicate(rF15402, sendOneSignalDerivativesVmLossPayment, mapF15402);
        break;
      case 'F15403':
        const rF15403 = parseEventBody<IDerivativesMarginCallLevel2Request>(message.eventCode, message.eventBody);
        if (rF15403 == null) {
          return;
        }
        checkDuplicate(rF15403, sendOneSignalDerivativesMarginCallLevel2, mapF15403);
        break;
      case 'F15404':
        const rF15404 = parseEventBody<IDerivativesMarginCallLevel3Request>(message.eventCode, message.eventBody);
        if (rF15404 == null) {
          return;
        }
        checkDuplicate(rF15404, sendOneSignalDerivativesMarginCallLevel3, mapF15404);
        break;
      case 'F15701':
        const rF15701 = parseEventBody<IDerivativesMarginDepositRequest>(message.eventCode, message.eventBody);
        if (rF15701 == null) {
          return;
        }
        checkDuplicate(rF15701, sendOneSignalDerivativesMarginDeposit, mapF15701);
        break;
      case 'F15702':
        const rF15702 = parseEventBody<IDerivativesMarginWithdrawalRequest>(message.eventCode, message.eventBody);
        if (rF15702 == null) {
          return;
        }
        checkDuplicate(rF15702, sendOneSignalDerivativesMarginWithdrawal, mapF15702);
        break;
      case 'F15303':
        const rF15303: IOrderStatusDerivativeRequest = JSON.parse(message.eventBody);
        checkDuplicate(rF15303, sendDataOrderStatusDerivative, mapF15303);
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
