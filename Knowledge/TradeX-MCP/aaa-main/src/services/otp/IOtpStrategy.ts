import { Kafka } from "tradex-common";
import ISendOtpStrategyReq from "../../models/request/ISendOtpStrategyReq";
import IVerifyOtpStrategyReq from "../../models/request/IVerifyOtpStrategyReq";
import ISendOtpStrategyRes from "../../models/response/ISendOtpStrategyRes";
import IVerifyOtpStrategyRes from "../../models/response/IVerifyOtpStrategyRes";

export interface IOtpStrategy {
  readonly txType: string;
  sendOtp(request: ISendOtpStrategyReq, msg: Kafka.IMessage): Promise<ISendOtpStrategyRes>;
  verifyOtp(request: IVerifyOtpStrategyReq, msg: Kafka.IMessage): Promise<IVerifyOtpStrategyRes>;
}
