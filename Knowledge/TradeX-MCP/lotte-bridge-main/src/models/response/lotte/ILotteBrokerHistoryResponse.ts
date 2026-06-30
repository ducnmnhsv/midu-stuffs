import { ILotteCommonResponse } from './ILotteCommonResponse';

export interface ILotteBrokerHistoryResponse extends ILotteCommonResponse {
  data_list: ILotteBrokerHistoryData[];
}

export interface ILotteBrokerHistoryData {
  seq_no: string;
  updated_date: string;
  account_number: string;
  account_name: string;
  previous_branch_broker: string;
  new_branch_broker: string;
  previous_broker: string;
  new_broker: string;
  previous_type_broker: string;
  new_type_broker: string;
  user_created: string;
  reason: string;
  broker_status: string;
  broker_update_date: string;
  bos_status: string;
  user_accepted?: string;
  accept_date?: string;
}

