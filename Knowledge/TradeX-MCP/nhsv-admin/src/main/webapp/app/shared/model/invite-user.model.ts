import dayjs from 'dayjs';
import { InviteStatusEnum } from 'app/shared/model/enumerations/invite-status-enum.model';

export interface IInviteUser {
  id?: number;
  login?: string | null;
  email?: string | null;
  status?: InviteStatusEnum | null;
  createdAt?: string | null;
  updatedAt?: string | null;
  createdId?: number | null;
  createdBy?: string | null;
  activationKey?: string | null;
  activationDate?: string | null;
  langKey?: string | null;
  authorities?: string | null;
}

export const defaultValue: Readonly<IInviteUser> = {};
