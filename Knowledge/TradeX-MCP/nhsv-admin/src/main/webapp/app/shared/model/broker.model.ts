import dayjs from 'dayjs';

export interface IBroker {
  id?: number;
  username?: string | null;
  fullname?: string | null;
  status?: boolean | null;
  totalChatRoom?: number | null;
  currentRank?: number | null;
  isDynamic?: boolean | null;
  email?: string | null;
  createdAt?: string | null;
  updatedAt?: string | null;
  deactivatedAt?: string | null;
  deactivatedBy?: string | null;
  invitedBy?: string | null;
}

export const defaultValue: Readonly<IBroker> = {
  status: false,
  isDynamic: false,
};
