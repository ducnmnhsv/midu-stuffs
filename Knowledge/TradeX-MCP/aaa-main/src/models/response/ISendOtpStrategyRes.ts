export default interface ISendOtpStrategyRes {
  otpId: string;
  expiredTime: string;
  resendRemaining: number;
  phoneNumber: string;
}
