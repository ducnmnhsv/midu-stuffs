import {Models} from "tradex-common";

export default interface IVerifyOtpReq extends Models.IDataRequest {
  otp_value?: string;
  mobile_otp?: string;
  macAddress?: string;
  platform?: string;
  osVersion?: string; 
  appVersion?: string;
  sourceIp?: string;
}
