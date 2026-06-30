import dayjs from 'dayjs';
import { IUser } from 'app/shared/model/user.model';

export interface ICopyPortfolio {
  id?: number;
  createdAt?: string;
  mlUserId?: IUser | null;
}

export const defaultValue: Readonly<ICopyPortfolio> = {};
