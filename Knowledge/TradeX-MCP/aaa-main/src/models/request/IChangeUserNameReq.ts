import { Models } from "tradex-common";

export default interface IChangeUserNameReq extends Models.IDataRequest {
  partnerId: string;
  userId: number;
  oldUsername: string;
  newUsername: string;
}