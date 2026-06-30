import { Models } from "tradex-common";

export default interface IAddNewKeyRequest extends Models.IDataRequest {
  namespaceId: number;
  key: string;
}
