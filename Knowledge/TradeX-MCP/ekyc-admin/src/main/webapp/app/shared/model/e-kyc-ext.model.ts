import { IEKyc } from 'app/shared/model/e-kyc.model';

export interface IEKycExt {
  id?: number;
  logId?: string | null;
  rawData?: string | null;
  eKyc?: IEKyc | null;
}

export const defaultValue: Readonly<IEKycExt> = {};
