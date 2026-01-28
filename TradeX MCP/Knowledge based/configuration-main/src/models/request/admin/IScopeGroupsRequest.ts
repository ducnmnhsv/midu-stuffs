import { Models } from "tradex-common";

export default interface IScopeGroupsRequest extends Models.IDataRequest {
  scopeGroupName?: string;
  lastSequence?: number;
  fetchCount?: number;
}

export interface ICreateScopeGroupRequest extends Models.IDataRequest {
  scopeGroupName: string;
  scopeIds: number[];
}

export interface IUpdateScopeGroupRequest extends Models.IDataRequest {
  scopeGroupId: number;
  scopeGroupName?: string;
  scopeIds?: number[];
}

export interface IFindByIdScopeGroupRequest extends Models.IDataRequest {
  scopeGroupId: number;
}

export type IDeleteScopeGroupRequest = IFindByIdScopeGroupRequest;
