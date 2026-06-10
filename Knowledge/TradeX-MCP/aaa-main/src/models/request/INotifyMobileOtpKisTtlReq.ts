import {Models} from "tradex-common";

export default interface INotifyMobileOtpKisTtlReq extends Models.IDataRequest {
  playerId?: string;
  forceSMS?: boolean;
  matrixId: number;
  macAddress?: string;
  platform?: string;
  osVersion?: string; 
  appVersion?: string;
  sourceIp?: string;
}

