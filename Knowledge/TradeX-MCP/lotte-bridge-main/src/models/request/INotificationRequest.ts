import { Models } from 'tradex-common';

export interface INotificationRequest extends Models.IDataRequest {
  subAccount: string;
  fromDate?: string;
  toDate?: string;
  nextKey?: string;
}

export interface IMaintenanceNotficationRequest {
  fromDate?: string;
  toDate?: string;
  nextKey?: string;
  rowCount?: number;
}
