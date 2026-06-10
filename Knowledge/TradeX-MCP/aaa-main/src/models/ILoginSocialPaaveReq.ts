export default interface ILoginSocialPaaveReq {
  username?: string;
  socialType?: string;
  socialToken?: string;
  systemName?: string;
  deviceId?: string;
  macAddress?: string;
  platform?: string;
  osVersion?: string; 
  appVersion?: string;
  sourceIp?: string;
  organization?: string;
}
