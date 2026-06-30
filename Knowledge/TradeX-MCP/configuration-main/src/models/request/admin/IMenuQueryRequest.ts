import { Models } from "tradex-common";

export default interface IMenuQueryRequest extends Models.IDataRequest {
  menuRoleIds: number[];
}
