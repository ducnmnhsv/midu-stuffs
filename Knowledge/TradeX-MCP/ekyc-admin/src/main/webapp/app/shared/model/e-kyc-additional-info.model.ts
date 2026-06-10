import { IEKyc } from 'app/shared/model/e-kyc.model';

export interface IEKycAdditionalInfo {
  id?: number;
  fullName?: string | null;
  birthDay?: string | null;
  nationality?: string | null;
  identifierId?: string | null;
  issueDate?: string | null;
  issuePlace?: string | null;
  permanentAddress?: string | null;
  contactAddress?: string | null;
  occupation?: string | null;
  position?: string | null;
  phoneNumber?: string | null;
  visaNo?: string | null;
  visaIssuePlace?: string | null;
  foreignResidence?: string | null;
  investmentGoal?: string | null;
  risk?: string | null;
  experienced?: boolean | null;
  eKyc?: IEKyc | null;
}

export const defaultValue: Readonly<IEKycAdditionalInfo> = {
  experienced: false,
};
