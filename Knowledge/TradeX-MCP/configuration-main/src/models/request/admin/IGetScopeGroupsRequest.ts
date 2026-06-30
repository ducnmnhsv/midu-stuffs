import { Models } from "tradex-common";

export default interface IGetScopeGroupsRequest extends Models.IDataRequest {
  scopeGroupName?: string;
  lastSequence?: number;
  fetchCount?: number;
}
