import { Request, Response } from 'express';

export interface IRequestContext {
  req: Request;
  res: Response;
  messageId: string;
  lang: string;
  rid?: string;
  txId?: string;
}
