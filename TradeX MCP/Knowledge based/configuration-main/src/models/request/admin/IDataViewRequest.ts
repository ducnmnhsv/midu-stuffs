import { Models } from "tradex-common";

export default interface IDataViewRequest extends Models.IDataRequest {
  code: string;
  fetchCount: number;
  lastSequence: number;
  [key: string]: any;
}
