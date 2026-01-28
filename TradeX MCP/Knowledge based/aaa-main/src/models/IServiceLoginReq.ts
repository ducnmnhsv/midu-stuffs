import { Models } from "tradex-common";

export default interface IServiceLoginReq extends Models.IDataRequest {
  username: string;
  password: string;
  systemName: string;
  headers?: any;
  device_id?: string;
  login_social_token?: string;
  login_social_type?: string;
  macAddress?: string;
  platform?: string;
  osVersion?: string; 
  appVersion?: string;
  sourceIp?: string;
}
