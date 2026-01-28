import { Models } from "tradex-common";

export default interface ISaveScopeRequest extends Models.IDataRequest {
  scopeId?: number;
  name: string;
  scopeGroupIds?: number[];
  uriPattern: string;
  forwardType?: string;
  forwardData?: Models.AAA.IForwardService | Models.AAA.IForwardConnection;
}
