import { TradexNotification } from 'tradex-common';

export default class DerivativesVmLossPayment implements TradexNotification.ITemplateData {
  acnt_no?: string;
  sub_no?: string;
  trd_amt?: string;
  trd_dt_fmt?: string;

  getTemplate(): string {
    return 'lottehpt_derivatives_vm_loss_payment';
  }
}
