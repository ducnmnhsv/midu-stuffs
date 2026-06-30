// ACC-029: Account history broker request
export interface ILotteBrokerHistoryRequest {
  account_number: string;
  from_date: string;
  to_date: string;
  previous_broker: string;
  new_broker: string;
  broker_status: string;
  status: string;
  hts_user_id: string;
}

