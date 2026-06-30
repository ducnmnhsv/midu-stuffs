import dayjs from 'dayjs';
import { StatusEnum } from 'app/shared/model/enumerations/status-enum.model';

export interface ICreatedChatRoom {
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
  approvedBy?: string | null;
  rejectReason?: string | null;
  totalView?: number | null;
  file?: any | null;
}

export const defaultValue: Readonly<ICreatedChatRoom> = {};
