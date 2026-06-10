import { Models } from 'tradex-common';

export interface IOtpInfoRequest extends Models.IDataRequest {
  macAddress?: string;
  platform?: string;
  osVersion?: string;
  appVersion?: string;
  sourceIp?: string;
}
