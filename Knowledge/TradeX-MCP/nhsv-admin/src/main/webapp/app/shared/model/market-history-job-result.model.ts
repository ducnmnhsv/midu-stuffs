import dayjs from 'dayjs';
import { IUser } from 'app/shared/model/user.model';

export interface IMarketHistoryJobResult {
  id?: number;
  isSuccess?: boolean | null;
  timeStart?: string | null;
  timeEnd?: string | null;
  error?: string | null;
  eventId?: string | null;
  symbols?: string | null;
  user?: IUser | null;
}

export const defaultValue: Readonly<IMarketHistoryJobResult> = {
  isSuccess: false,
};
