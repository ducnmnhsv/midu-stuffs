import {Models} from "tradex-common";

export default interface INotifyMobileOtpReq extends Models.IDataRequest {
  playerId?: string;
  forceSMS?: boolean;
  macAddress?: string;
  platform?: string;
  osVersion?: string; 
  appVersion?: string;
  sourceIp?: string;
}
