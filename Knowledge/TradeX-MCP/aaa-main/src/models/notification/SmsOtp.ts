import {TradexNotification} from 'tradex-common';

export default class SmsOtp
  implements TradexNotification.ITemplateData {
  public otp: string;

  public getTemplate(): string {
    return 'sms_otp';
  }
}