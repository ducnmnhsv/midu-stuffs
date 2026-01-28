import {TradexNotification, Utils} from 'tradex-common';
import * as uuid from 'uuid';


let sendNotification: TradexNotification.SendNotification = null;

export function getSendNotification() {
  if (sendNotification == null) {
    sendNotification = TradexNotification.getInstance();
  }
  return sendNotification;
}


export function notifyOneSignal(
  data: TradexNotification.ITemplateData,
  username: string,
  userId: number | undefined,
  domain: string,
  extraFilter?: TradexNotification.IFilter [],
  playerId?: string
) {
  const conf: TradexNotification.OneSignalConfiguration = new TradexNotification.OneSignalConfiguration();
  conf.domain = domain;

  if (!Utils.isEmpty(playerId)) {
    const includePlayerIds = [];
    includePlayerIds.push(playerId);
    conf.include_player_ids = includePlayerIds;
  } else {
    conf.filters = [];
    if (extraFilter != null) {
      conf.filters = conf.filters.concat(extraFilter);
    }
    const filter: TradexNotification.IFilter = userId == null ? {
      field: 'tag',
      key: 'username',
      relation: '=',
      value: `${username.toLowerCase()}`,
    } : {
      field: 'tag',
      key: 'userid',
      relation: '=',
      value: `${userId}`,
    };
    conf.filters.push(filter);
  }

  getSendNotification().sendPushNotification(uuid(), conf, data);
}

export function notifyOneSignalNhsv(
  data: TradexNotification.ITemplateData,
  domain: string,
  extraFilter?: TradexNotification.IFilter [],
  playerId?: string
) {
  const conf: TradexNotification.OneSignalConfiguration = new TradexNotification.OneSignalConfiguration();
  conf.domain = domain;

  if (!Utils.isEmpty(playerId)) {
    const includePlayerIds = [];
    includePlayerIds.push(playerId);
    conf.include_player_ids = includePlayerIds;
  } else {
    conf.filters = [];
    conf.filters = conf.filters.concat(extraFilter);
  }

  getSendNotification().sendPushNotification(uuid(), conf, data);
}

export function sendSms(
  data: TradexNotification.ITemplateData,
  phoneNumber: string,
  domain: string,
  locale: string
) {
  const conf: TradexNotification.SmsConfiguration = new TradexNotification.SmsConfiguration();
  conf.domain = domain;
  conf.phoneNumber = phoneNumber;

  getSendNotification().sendSms(uuid(), conf, data, locale);
}
