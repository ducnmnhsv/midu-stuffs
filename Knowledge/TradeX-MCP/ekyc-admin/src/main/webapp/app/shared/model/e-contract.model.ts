import dayjs from 'dayjs';
import { IEKyc } from 'app/shared/model/e-kyc.model';

export interface IEContract {
  id?: number;
  refId?: string;
  envelopeId?: string;
  identifierId?: string;
  templateId?: string;
  alias?: string | null;
  companyType?: string;
  createdAt?: string;
  updatedAt?: string | null;
  eKyc?: IEKyc | null;
}

export const defaultValue: Readonly<IEContract> = {};
