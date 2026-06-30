import { Models } from "tradex-common";

export default interface IGetAllKeysByNamespaceRequest
  extends Models.IDataRequest {
  namespaceId: number;
  fetchCount?: number;
  keyword?: string;
  lastKey?: string;
}
