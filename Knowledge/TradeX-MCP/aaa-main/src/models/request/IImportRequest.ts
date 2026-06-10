import {Models} from "tradex-common";

export default interface IImportRequest extends Models.IDataRequest {
  url: string //url to get database json file
}
