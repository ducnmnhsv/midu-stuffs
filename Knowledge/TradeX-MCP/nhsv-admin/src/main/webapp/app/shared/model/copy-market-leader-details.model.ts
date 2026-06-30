import dayjs from 'dayjs';
import { IUser } from 'app/shared/model/user.model';

export interface ICopyMarketLeaderDetails {
  id?: number;
  createdAt?: string;
  updatedAt?: string | null;
  type?: string;
  label?: string;
  key?: string;
  value?: string;
  mlUserId?: IUser | null;
}

export const defaultValue: Readonly<ICopyMarketLeaderDetails> = {};
