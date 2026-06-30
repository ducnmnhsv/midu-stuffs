import { Models } from "tradex-common";

export default interface IUploadLangResourceRequest
  extends Models.IDataRequest {
  namespaceId: number;
  lang: string;
  version?: string;
}
