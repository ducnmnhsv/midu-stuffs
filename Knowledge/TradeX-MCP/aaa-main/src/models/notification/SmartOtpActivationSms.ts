import { TradexNotification } from 'tradex-common';

export default class SmartOtpActivationSms
  implements TradexNotification.ITemplateData {
  public otp: string;

  public getTemplate(): string {
    return 'sms_smart_otp_activation';
  }
}