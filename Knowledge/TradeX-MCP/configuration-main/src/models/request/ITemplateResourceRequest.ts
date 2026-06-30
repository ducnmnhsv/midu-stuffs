import { Models } from "tradex-common";

export default interface ITemplateResourceRequest extends Models.IDataRequest {
  msNames?: string[];
}
