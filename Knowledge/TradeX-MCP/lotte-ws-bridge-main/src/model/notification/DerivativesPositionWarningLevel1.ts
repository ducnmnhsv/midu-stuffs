import { TradexNotification } from 'tradex-common';

export default class DerivativesPositionWarningLevel1 implements TradexNotification.ITemplateData {
  acnt_no?: string;
  sub_no?: string;
  W1?: string;
  commd_cd?: string;
  date_fmt?: string;

  getTemplate(): string {
    return 'lottehpt_derivatives_position_warning_level_1';
  }
}
