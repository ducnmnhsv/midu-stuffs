import { IChatRoom } from 'app/shared/model/chat-room.model';

export interface ISocialLink {
  id?: number;
  type?: string | null;
  link?: string | null;
  chatRoom?: IChatRoom | null;
}

export const defaultValue: Readonly<ISocialLink> = {};
