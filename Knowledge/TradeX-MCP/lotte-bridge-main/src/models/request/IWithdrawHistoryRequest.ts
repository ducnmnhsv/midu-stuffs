import { Models } from 'tradex-common';
import { WithdrawStatus } from '../../constants/enum';

export interface IWithdrawHistoryRequest extends Models.IDataRequest {
  status: WithdrawStatus;
  accountNumber: string;
  subNumber: string;
  fromDate: string;
  toDate: string;
  next: string;
  fetchCount: number;
}
