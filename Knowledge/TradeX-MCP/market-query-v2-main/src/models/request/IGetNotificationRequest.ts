export interface IGetNotificationRequest {
  type?: string;
  keyword?: string;
  fromDate?: string;
  toDate?: string;
  pageSize?: number;
  pageNumber?: number;
}
