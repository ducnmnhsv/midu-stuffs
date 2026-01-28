import { Models } from 'tradex-common';
import { ConfirmStatus } from '../../constants/enum';

export interface IGetTransferStockConfirmRequest extends Models.IDataRequest {
  status: ConfirmStatus;
  fromDate: string;
  toDate: string;
  accountNumber: string;
  subNumber: string;
  nextKey: string;
  stockCode: string;
  fetchCount: number;
}
