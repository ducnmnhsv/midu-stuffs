export default interface ILoginGoogleReq {
  accessToken?: string;
  idToken?: string; 
  username?: string;
  login_social_token?: string;
  socialType?: string;
  device_id?: string;
  password?: string;
  macAddress?: string;
  platform?: string;
  osVersion?: string; 
  appVersion?: string;
  sourceIp?: string;
}
