import { IEKyc } from 'app/shared/model/e-kyc.model';

export interface IEKycBankList {
  id?: number;
  bankId?: string | null;
  bankName?: string | null;
  bankAccNo?: string | null;
  ownerName?: string | null;
  branchId?: string | null;
  eKyc?: IEKyc | null;
}

export const defaultValue: Readonly<IEKycBankList> = {};
