import {
  ILotteMaintenanceNotificationData,
  ILotteMaintenanceNotificationResponse,
  ILotteotificationData,
  ILotteotificationResponse,
} from './lotte/ILotteotificationResponse';

export interface INotificationResponse {
  seq: string;
  sendDateTime: string;
  author: string;
  title: string;
  shortTitle: string;
  content: string;
  shortContent: string;
  nextKey: string;
}

export function toNotificationResponse(response: ILotteotificationResponse): INotificationResponse[] {
  return response.data_list.map((item: ILotteotificationData) => {
    return {
      seq: item.seq_no,
      sendDateTime: item.send_dt + ' ' + item.send_time,
      author: item.work_mn,
      title: item.title,
      shortTitle: item.short_title,
      content: item.notif_ms,
      shortContent: item.short_msg,
      nextKey: item.next_key,
    };
  });
}

export interface IMaintenanceNotficationResponse {
  seq: string;
  createdDateTime: string;
  title: string;
  shortTitle: string;
  content: string;
  shortContent: string;
  author: string;
}

export function toMaintenanceNotficationResponse(
  response: ILotteMaintenanceNotificationResponse
): IMaintenanceNotficationResponse[] {
  return response.data_list.map((item: ILotteMaintenanceNotificationData) => {
    return {
      seq: item.seq_no,
      createdDateTime: item.date + ' ' + item.hour,
      title: item.title,
      shortTitle: item.short_title,
      content: item.content,
      shortContent: item.short_content,
      author: item.work_mn,
    };
  });
}
