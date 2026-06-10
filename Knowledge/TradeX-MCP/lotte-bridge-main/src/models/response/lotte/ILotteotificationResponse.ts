import { ILotteCommonResponse } from './ILotteCommonResponse';

export interface ILotteotificationResponse extends ILotteCommonResponse {
  data_list?: ILotteotificationData[];
}

export interface ILotteotificationData {
  acnt_no?: string;
  sub_no?: string;
  title?: string;
  send_dt?: string;
  send_time?: string;
  work_mn?: string;
  notif_ms?: string;
  seq_no?: string;
  short_title?: string;
  short_msg?: string;
  next_key?: string;
}

export interface ILotteMaintenanceNotificationResponse extends ILotteCommonResponse {
  data_list?: ILotteMaintenanceNotificationData[];
}

export interface ILotteMaintenanceNotificationData {
  seq_no?: string;
  title?: string;
  short_title?: string;
  content?: string;
  short_content?: string;
  work_mn?: string;
  date?: string;
  hour?: string;
}
