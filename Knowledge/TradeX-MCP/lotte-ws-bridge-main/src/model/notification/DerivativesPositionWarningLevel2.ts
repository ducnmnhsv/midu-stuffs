import { TradexNotification } from 'tradex-common';

export default class DerivativesPositionWarningLevel2 implements TradexNotification.ITemplateData {
  acnt_no?: string;
  sub_no?: string;
  W2?: string;
  commd_cd?: string;
  date_fmt?: string;

  getTemplate(): string {
    return 'lottehpt_derivatives_position_warning_level_2';
  }
}
