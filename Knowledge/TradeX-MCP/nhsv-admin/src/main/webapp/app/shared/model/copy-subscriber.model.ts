import dayjs from 'dayjs';
import { IUser } from 'app/shared/model/user.model';
import { OrderSetTypeEnum } from 'app/shared/model/enumerations/order-set-type-enum.model';

export interface ICopySubscriber {
  id?: number;
  accountNumber?: string;
  subNumber?: string;
  userName?: string;
  allocatedRatio?: number;
  orderSetType?: OrderSetTypeEnum | null;
  createdAt?: string;
  updatedAt?: string | null;
  mlUserId?: IUser | null;
}

export const defaultValue: Readonly<ICopySubscriber> = {};
