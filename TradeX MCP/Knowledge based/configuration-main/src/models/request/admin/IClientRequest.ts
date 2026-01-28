export default interface IClientRequest {
  domain: string;
  isFullData?: boolean;
  lastSequence?: number;
  fetchCount?: number;
}

export interface IClientIdRequest {
  id: number;
}

export interface IAddClientRequest {
  userId: number;
  clientId: string;
  clientSecret: string;
  description?: string;
  domain: string;
  loginMethodIds?: number[];
}

export interface IUpdateClientRequest {
  id: number;
  userId?: number;
  clientId?: string;
  clientSecret?: string;
  description?: string;
  domain?: string;
  loginMethodIds?: number[];
}
