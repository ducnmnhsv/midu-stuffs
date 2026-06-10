import { TradexNotification } from 'tradex-common';

export default class ChangePassword implements TradexNotification.ITemplateData {
  flag?: string;

  getTemplate(): string {
    return 'lottehpt_change_password';
  }
}
