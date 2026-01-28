import {Models} from "tradex-common";

export default interface IPhoneNumberReq extends Models.IDataRequest {
  accountNumber: string;
  macAddress?: string;
  platform?: string;
  osVersion?: string; 
  appVersion?: string;
  sourceIp?: string;
}
