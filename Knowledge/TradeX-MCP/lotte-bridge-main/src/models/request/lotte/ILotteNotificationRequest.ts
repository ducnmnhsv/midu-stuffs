export interface ILotteNotificationRequest {
  acnt_no: string;
  sub_no: string;
  from_dt: string;
  to_dt: string;
  type: string;
  next_key: string;
  lang_code: string;
}

export interface ILotteMaintenanceNotificationRequest {
  from_date: string;
  to_date: string;
  next_key: string;
  row_count: string;
}
