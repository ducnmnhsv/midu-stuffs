import dayjs from 'dayjs';
import { EkycType } from 'app/shared/model/enumerations/ekyc-type.model';
import { Status } from 'app/shared/model/enumerations/status.model';

export interface IEKyc {
  id?: number;
  identifierId?: string;
  fullName?: string;
  phoneNo?: string | null;
  gender?: string | null;
  type?: EkycType;
  birthDay?: string;
  expiredDate?: string;
  issueDate?: string;
  issuePlace?: string;
  address?: string;
  occupation?: string | null;
  homeTown?: string | null;
  permanentProvince?: string | null;
  permanentDistrict?: string | null;
  permanentAddress?: string | null;
  contactProvince?: string | null;
  contactDistrict?: string | null;
  contactAddress?: string | null;
  email?: string;
  referrerIdName?: string | null;
  referrerBranch?: string | null;
  bankAccount?: string | null;
  accountName?: string | null;
  bankName?: string | null;
  branch?: string | null;
  nationality?: string | null;
  status?: Status;
  frontImageUrl?: string;
  backImageUrl?: string;
  portraitImageUrl?: string | null;
  signatureImageUrl?: string | null;
  tradingCodeImageUrl?: string | null;
  isMargin?: boolean | null;
  matchingRate?: number | null;
  updatedAt?: string | null;
  createdAt?: string | null;
  province?: string | null;
  idList?: number[] | null;
  paramString?: string | null;
}

export const defaultValue: Readonly<IEKyc> = {
  isMargin: false,
};
