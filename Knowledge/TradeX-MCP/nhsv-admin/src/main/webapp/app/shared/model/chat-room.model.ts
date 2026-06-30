import dayjs from 'dayjs';
import { ISocialLink } from 'app/shared/model/social-link.model';
import { StatusEnum } from 'app/shared/model/enumerations/status-enum.model';
import { ActionEnum } from 'app/shared/model/enumerations/action-enum.model';

export interface IChatRoom {
  id?: number;
  groupName?: string | null;
  groupOwner?: string | null;
  introduction?: string | null;
  photo?: string | null;
  brokerName?: string | null;
  brokerContact?: string | null;
  status?: StatusEnum | null;
  createdBy?: string | null;
  createdAt?: string | null;
  updatedAt?: string | null;
  approvedAt?: string | null;
  rejectedAt?: string | null;
  rejectReason?: string | null;
  approvedBy?: string | null;
  rejectedBy?: string | null;
  action?: ActionEnum | null;
  file?: any | null;
  socialLinks?: ISocialLink[] | null;
}

export const defaultValue: Readonly<IChatRoom> = {};
