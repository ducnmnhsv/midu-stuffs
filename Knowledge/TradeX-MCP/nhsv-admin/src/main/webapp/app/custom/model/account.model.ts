import { ICopyMarketLeaderDetails } from 'app/shared/model/copy-market-leader-details.model';

export interface IAccount {
  id?: any;
  login?: string;
  fullName?: string;
  email?: string;
  imageUrl?: string;
  activated?: boolean;
  langKey?: string;
  createdBy?: string;
  authorities?: any[];
  createdDate?: Date | null;
  lastModifiedBy?: string;
  lastModifiedDate?: Date | null;
  deactivatedAt?: Date | null;
  deactivatedBy?: string;
  invitedBy?: string;
  introduction?: string;
  photo?: string;
  photoLink?: string;
  copyMarketLeaderDetailsDTO?: ICopyMarketLeaderDetails[];
}
