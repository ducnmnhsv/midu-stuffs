import dayjs from 'dayjs';

export interface IRecentViewChatRoom {
  id?: number;
  userId?: number | null;
  chatRoomId?: number | null;
  createdAt?: string | null;
  updatedAt?: string | null;
  deletedAt?: string | null;
}

export const defaultValue: Readonly<IRecentViewChatRoom> = {};
