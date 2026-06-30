import dayjs from 'dayjs';
import { IUser } from 'app/shared/model/user.model';

export interface ICopyPortfolioHistory {
  id?: number;
  createdAt?: string;
  mlUserId?: IUser | null;
}

export const defaultValue: Readonly<ICopyPortfolioHistory> = {};
