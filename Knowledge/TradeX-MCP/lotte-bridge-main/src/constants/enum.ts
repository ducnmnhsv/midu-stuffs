export enum LOTTE_LANG_CODE {
  vi = 'V',
  en = 'E',
  ko = 'K',
}

export const SELL_BUY_TYPE = {
  SELL: 'SELL',
  BUY: 'BUY',
};

export const ORDER_TYPE = {
  LO: '01',
  MP: '02',
  ATO: '03',
  ATC: '04',
  AON: '05',
  BIG_LOT: '06',
  MOK: '07',
  MAK: '08',
  MTL: '09',
  IO: '10',
  SO_GREATER: '11',
  SO_LESS: '12',
  SBO: '13',
  OBO: '14',
  PLO: '15',
  ALL: '%',
};

export const MARKET_TYPE = {
  ALL: '%',
  HOSE: '1',
  HNX: '2',
  UPCOM: '3',
};

export const SORT_TYPE = {
  ASC: 'A',
  DESC: 'D',
};

export const MATCH_TYPE = {
  ALL: '%',
  MATCHED: '1',
  UNMATCHED: '2',
};

export const SELL_BUY_TYPE_LOTTE = {
  ALL: '%',
  SELL: '1',
  BUY: '2',
  MODIFY_SELL: '3',
  MODIFY_BUY: '4',
  CANCEL_SELL: '5',
  CANCEL_BUY: '6',
};

export const ORDER_STATUS = {
  ALL: '%',
  RECEIPT: '0',
  SEND: '1',
  ORDER_CONFIRM: '2',
  RECEIPT_CONFIRM: '3',
  FULL_FILLED: '4',
  PARTIAL_FILLED: '5',
  REJECT: 'X',
};

export const ORDER_MODIFY_CANCEL_TYPE = {
  ALL: '%',
  NORMAL: '0',
  PARTIAL_CORRECTION: '1',
  ALL_CORRECTION: '2',
  PARTIAL_CANCEL: '3',
  ALL_CANCEL: '4',
};

export const CHANNEL_TYPE = {
  '00': 'BOS',
  '01': 'Phone',
  '03': 'WTS',
  '04': 'HTS',
  '06': 'MTS-iOS',
  '07': 'MTS-iPad',
  '08': 'MTS-Android',
  '20': 'OMS Order',
  '30': 'API',
  '%': 'ALL',
};

export const MARKET_TYPE_LOTTE = {
  '01': 'Trung tâm HCM',
  '02': 'Lô lẻ HCM',
  '03': 'Trung tâm HN',
  '04': 'Lô lẻ HN',
  '05': 'UPCOM',
};

export const SELL_BUY_TYPE_ADVANCED = {
  ALL: '3',
  SELL: '1',
  BUY: '2',
};

export const MARKET_TYPE_ADVANCED = {
  HOSE: '1',
  NHX: '2', // HNX = HASTC core
  DCCNY: '3',
  UPCOM: '4',
  ALL: '%',
};

export const WITHDRAW_STATUS = {
  PENDING: 'c',
  CANCELLED: 'd',
  APPROVED: 'e',
};

export const RIGHT_TYPE = {
  Subscription: '1',
  Bond: '4',
};

export const MARKET_LIST = ['HOSE', 'HNX', 'UPCOM'];

export enum LOTTE_MARKET_TYPE {
  HOSE = 'hsx',
  HNX = 'hnx',
  UPCOM = 'upcom',
}

export enum LOTTE_INDEX {
  HOSE = '001',
  HNX = '101',
  UPCOM = '301',
}

export enum LOTTE_MARKET_TYPE_EXCHANGE_INFO {
  ALL = '%',
  HOSE = 'hose',
  HNX = 'hnx',
  UPCOM = 'upcom',
}

export const VSD_STATUS = {
  '1': 'PENDING',
  '2': 'APPROVED',
  '3': 'REJECTED',
  '': 'UNKNOWN',
};

export const MARKET_TYPE_PERIOD = {
  HOSE: '0',
  HNX: '1',
  UPCOM: '2',
  ALL: '3',
};

export const RANKING = {
  UP: '0',
  DOWN: '1',
};

export const CONFIRM_STATUS = {
  Y: 'Y',
  N: 'N',
  ALL: '%',
};

export const SUB_TYPE = {
  NORMAL: '0',
  MARGIN: '1',
  BOND: '2',
};

export const CASH_DEPOSIT_HISTORY_TYPE = {
  ALL: '%',
  DEPOSIT: '01',
  WITHDRAW: '02',
  RIGHTS_CASH: '03',
  SETTLEMENT: '04',
  TAX: '05',
  ADVANCED_CASH: '06',
  PLEDGE_LOAN: '07',
  PURCHASE_LOAN: '08',
  MARGIN_LOAN: '09',
  DEPOSITORY_FEE: '10',
  EXTERNAL_LOAN: '11',
}

export enum AccountType {
  INDIVIDUAL = 'INDIVIDUAL',
  INSTITUTION = 'INSTITUTION',
}

export const CHANGE_BROKER_REASON = {
  SELF_TRADING: { code: 'SELF_TRADING', label: 'Muốn tự giao dịch, không cần môi giới hỗ trợ' },
  CHANGE_BROKER: { code: 'CHANGE_BROKER', label: 'Muốn chuyển sang môi giới khác' },
  LOW_QUALITY: { code: 'LOW_QUALITY', label: 'Không hài lòng với chất lượng tư vấn, khuyến nghị đầu tư' },
  LOW_SUPPORT: { code: 'LOW_SUPPORT', label: 'Môi giới hiện tại ít liên lạc / hỗ trợ chưa kịp thời' },
  BROKER_MOVED: { code: 'BROKER_MOVED', label: 'Môi giới hiện tại thay đổi nơi làm việc / không còn làm tại công ty' },
  OTHER: { code: 'OTHER', label: 'Lý do cá nhân khác' },
} as const;

export function getChangeBrokerReasonLabel(code: string): string {
  const reason = Object.values(CHANGE_BROKER_REASON).find(r => r.code === code);
  return reason?.label || code;
}

export function getChangeBrokerReasonCodes(): string[] {
  return Object.values(CHANGE_BROKER_REASON).map(r => r.code);
}

export const TradingSession = {
  P: 'ATO',
  O: 'CONTINUOUS',
  I: 'INTERMISSION',
  '2': 'INTERMISSION',
  A: 'ATC',
  C: 'PLO',
  K: 'CLOSED',
  G: 'CLOSED',
} as const;

export const NOTIFICATION_TYPE = {
  ALL: '%',
  ACCOUNT: '01',
  STOCK: '02',
  ORDER: '03',
  LOAN: '04',
  CASH: '07',
  OTHER: '09',
} as const;

export type SellBuyType = typeof SELL_BUY_TYPE[keyof typeof SELL_BUY_TYPE];

export type OrderType = typeof ORDER_TYPE[keyof typeof ORDER_TYPE];

export type MarketType = typeof MARKET_TYPE[keyof typeof MARKET_TYPE];

export type SortType = typeof SORT_TYPE[keyof typeof SORT_TYPE];

export type MatchType = typeof MATCH_TYPE[keyof typeof MATCH_TYPE];

export type SellBuyTypeLotte = typeof SELL_BUY_TYPE_LOTTE[keyof typeof SELL_BUY_TYPE_LOTTE];

export type OrderStatus = typeof ORDER_STATUS[keyof typeof ORDER_STATUS];

export type OrderModifyCancelType = typeof ORDER_MODIFY_CANCEL_TYPE[keyof typeof ORDER_MODIFY_CANCEL_TYPE];

export type SellBuyTypeAdvanced = typeof SELL_BUY_TYPE_ADVANCED[keyof typeof SELL_BUY_TYPE_ADVANCED];

export type MarketTypeAdvanced = typeof MARKET_TYPE_ADVANCED[keyof typeof MARKET_TYPE_ADVANCED];

export type WithdrawStatus = typeof WITHDRAW_STATUS[keyof typeof WITHDRAW_STATUS];

export type RightType = typeof RIGHT_TYPE[keyof typeof RIGHT_TYPE];

export type ConfirmStatus = typeof CONFIRM_STATUS[keyof typeof CONFIRM_STATUS];

export type SubType = typeof SUB_TYPE[keyof typeof SUB_TYPE];

export type CashDepositHistoryType = typeof CASH_DEPOSIT_HISTORY_TYPE[keyof typeof CASH_DEPOSIT_HISTORY_TYPE];

export type NotificationType = typeof NOTIFICATION_TYPE[keyof typeof NOTIFICATION_TYPE];

export type ChangeBrokerReasonType = typeof CHANGE_BROKER_REASON[keyof typeof CHANGE_BROKER_REASON]['code'];

export function getKeyByValue(value: string | number, obj: object) {
  const indexOfS = Object.values(obj).indexOf(value);
  const key = Object.keys(obj)[indexOfS];
  return key;
}
