import { Models } from "tradex-common";

export interface INotifyOtpPartner extends Models.IDataRequest {
  forceSMS?: boolean;
  matrixId: number;
  partnerId: string;
  rid?: string;
}