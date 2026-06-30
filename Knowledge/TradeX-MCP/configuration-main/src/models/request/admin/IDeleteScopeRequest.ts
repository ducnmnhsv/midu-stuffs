import { Models } from "tradex-common";

export default interface IDeleteScopeRequest extends Models.IDataRequest {
  scopeId: number;
}
