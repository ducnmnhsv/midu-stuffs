import { Models } from "tradex-common";

export default interface IDeleteKeyRequest extends Models.IDataRequest {
  namespaceId: number;
  keyId: number;
}
