export interface IGetNotificationResponse {
  sendDate: string;
  sendTime: string;
  author: string;
  title: string;
  content: string;
}

export interface IGetNotification {
  seq: number;
  title: string;
  content: string;
  date: string;
  time: string;
  writer: string;
  type: string;
}
