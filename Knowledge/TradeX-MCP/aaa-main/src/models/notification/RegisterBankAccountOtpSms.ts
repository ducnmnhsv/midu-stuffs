import { TradexNotification } from 'tradex-common';

export default class RegisterBankAccountOtpSms
  implements TradexNotification.ITemplateData {
  public otp: string;

  public getTemplate(): string {
    return 'sms_register_bank_account_otp';
  }
}
