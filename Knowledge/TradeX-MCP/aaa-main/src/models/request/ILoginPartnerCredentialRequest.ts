export interface ILoginPartnerCredentialRequest {
  partnerId: string;
  partner: ILoginRequest;
  paave: IBaseLoginRequest;
  sessionTimeInMinute?: number; //allow users to choose login in a short period
  session_time_in_minute?: number; //allow users to choose login in a short period
  macAddress?: string;
  platform?: string;
  osVersion?: string;
  appVersion?: string;
  sourceIp?: string;
  headers?: any;
  deviceId?: string;
  rid?: string;
  infoAccessGranted?: boolean;
}

export interface IBaseLoginRequest {
  clientId: string;
  clientSecret: string;
  grantType: string;  
}

export interface ILoginRequest extends IBaseLoginRequest {
  username: string;
  password: string;
}