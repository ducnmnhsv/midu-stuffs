import dayjs from 'dayjs';
import { IEKyc } from 'app/shared/model/e-kyc.model';

export interface IEKycCreatorStatus {
  id?: number;
  status?: string | null;
  reason?: string | null;
  updatedAt?: string | null;
  updatedBy?: string | null;
  fullResult?: string | null;
  eKyc?: IEKyc | null;
}

export const defaultValue: Readonly<IEKycCreatorStatus> = {};
