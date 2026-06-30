import { AccountType } from '../../constants/enum';

export interface IAccountInfoResponse {
  email?: string;
  address?: string;
  groupType?: string;
  agencyCode?: string;
  agencyName?: string;
  agencyId?: string;
  accountType?: AccountType;
  dateOfBirth?: string;
  phoneNumber?: string;
  agencyBranch?: string;
  customerName?: string;
  identifierNumber?: string;
  representativeName?: string;
  identifierIssueDate?: string;
  identifierExpireDate?: string;
  representativeEmail?: string;
  issuePlace?: string;
  isForeignCustomer?: boolean | null;
  representativePhoneNumber?: string;
  representativeIdentifierNumber?: string;
  taxCode?: string;
  username?: string;
}
