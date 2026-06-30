import { Models } from "tradex-common";

export default interface ISendOtpStrategyReq extends Models.IDataRequest {
  txType: string;
}
