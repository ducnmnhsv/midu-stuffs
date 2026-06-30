import { Models } from "tradex-common";

export default interface IUpdateLangByKeyRequest extends Models.IDataRequest {
  keyId: number;
  lang: string;
  value: string;
}
