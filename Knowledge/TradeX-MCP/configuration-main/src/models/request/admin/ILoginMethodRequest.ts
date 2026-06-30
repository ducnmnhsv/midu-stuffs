export default interface ILoginMethodRequest {
  fetchCount?: number;
  lastSequence?: number;
}

export interface ILoginMethodIdRequest {
  id: number;
}

export interface IAddLoginMethodRequest {
  serviceCode: string;
  grantType: string;
  msName: string;
  isDefault?: boolean;
  msUri?: string;
  scopeGroupIds?: number[];
}

export interface IUpdateLoginMethodRequest {
  id: number;
  serviceCode?: string;
  grantType?: string;
  msName?: string;
  msUri?: string;
  isDefault?: boolean;
  scopeGroupIds?: number[];
}
