export default interface ILoginDomainReq {
  username: string;
  domain: string;
  accountNumbers?: string[];
  macAddress?: string;
  platform?: string;
  osVersion?: string; 
  appVersion?: string;
  sourceIp?: string;
}
