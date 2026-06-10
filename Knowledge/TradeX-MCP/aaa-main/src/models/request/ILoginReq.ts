import { Models } from "tradex-common";

export default interface ILoginReq extends Models.IDataRequest {
  grant_type: string;
  client_id: string;
  client_secret: string;
  username: string; // password, password_otp
  password: string; // password, password_otp
  sec_code?: string; // password_otp
  access_token?: string; // login access_domain, access_google, access_facebook
  domain?: string; // login with access_domain
  id_token?: string; // login access_google
  remember_me?: boolean; // login password_tradex
  data?: string; // login password_ca
  mobileLogin?: boolean; // password_otp
  macAddress?: string;
  platform?: string;
  osVersion?: string;
  appVersion?: string;
  sourceIp?: string;
  headers?: any;
  device_id?: string;
  login_social_token?: string;
  login_social_type?: string;
  session_time_in_minute?: number; //allow users to choose login in a short period
  sessionTimeInMinute?: number; //allow users to choose login in a short period
  orgLoginToken?: string;
  organization?: string;
}
