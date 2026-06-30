import { Models } from 'tradex-common';
import { ChangeBrokerReasonType } from '../../constants/enum';

export interface IChangeBrokerRequest extends Models.IDataRequest {
  accountNumber: string;
  newBrokerId: string;
  reason: ChangeBrokerReasonType;
  note?: string;
}

