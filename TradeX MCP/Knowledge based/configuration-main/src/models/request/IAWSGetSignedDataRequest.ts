import { Models } from "tradex-common";

export default interface IAWSGetSignedDataRequest extends Models.IDataRequest {
  key: string;
  serviceName?: string;
}
