import dayjs from 'dayjs';
import { IEContract } from 'app/shared/model/e-contract.model';

export interface IEContractInfo {
  id?: number;
  createdAt?: string;
  updatedAt?: string | null;
  templateId?: string;
  requestData?: string;
  contactId?: string | null;
  customerSignatueStatus?: string | null;
  securitiesSignatureStatus?: string | null;
  contractStatus?: string | null;
  signFileContent?: string | null;
  contractFileContent?: string | null;
  eContract?: IEContract | null;
}

export const defaultValue: Readonly<IEContractInfo> = {};
