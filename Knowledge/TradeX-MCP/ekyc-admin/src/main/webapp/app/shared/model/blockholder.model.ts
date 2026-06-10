import { IEKycAdditionalInfo } from 'app/shared/model/e-kyc-additional-info.model';

export interface IBlockholder {
  id?: number;
  companyName?: string | null;
  stock?: string | null;
  position?: string | null;
  eKycAdditionalInfo?: IEKycAdditionalInfo | null;
}

export const defaultValue: Readonly<IBlockholder> = {};
