import {TradexNotification} from 'tradex-common';

export default class MobileOtpNotification
  implements TradexNotification.ITemplateData {
  public smartOtp: string;
  public username: string;
  public userId?: number;
  public accountNumber: string;
  public method: string = 'MOBILE_OTP';

  public getTemplate(): string {
    return 'smart_otp_notification';
  }
}