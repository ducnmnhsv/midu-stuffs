export interface ILotteChangeBrokerRequest {
  account_number: string;
  account_name: string;
  previous_broker: string;
  new_broker: string;
  reason: string;
  hts_user_id: string;
}
