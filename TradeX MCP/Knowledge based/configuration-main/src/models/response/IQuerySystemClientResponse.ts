import { IClientResponse } from "./IClientResponse";

export interface IQuerySystemClientResponse {
  clients?: IClientResponse[];
  lastQueriedTime?: string;
  [k: string]: any;
}
