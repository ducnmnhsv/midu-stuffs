import { Models } from 'tradex-common';

export interface INotificationStatusRequest extends Models.IDataRequest {
  notification: string;
}
