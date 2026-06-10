export default interface ILoginTechxReq {
  username: string;
  password: string;
  macAddress?: string;
  platform?: string;
  osVersion?: string; 
  appVersion?: string;
  sourceIp?: string;
}
