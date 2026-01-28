import { Models } from "tradex-common";

export default interface IGetScopesRequest extends Models.IDataRequest {
  name?: string;
  scopeGroupId?: number;
  uriPattern?: string;
  forwardType?: string;
  lastSequence?: number;
  fetchCount?: number;
}
