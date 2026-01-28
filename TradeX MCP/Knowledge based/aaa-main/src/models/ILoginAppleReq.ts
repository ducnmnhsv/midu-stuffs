export default interface ILoginAppleReq {
  socialType?: string;
  device_id?: string;
  password?: string;
  macAddress?: string;
  platform?: string;
  osVersion?: string; 
  appVersion?: string;
  sourceIp?: string;
}
