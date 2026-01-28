import FieldModel from "./FieldModel";
import {BaseModel} from "./BaseModel";
import {IUserInfo} from "../response/ILoginRes";
import {Models} from "tradex-common";

export interface IInfo {
  userInfo: IUserInfo;
  userData: Models.IUserData;
  registerMobileOtp: boolean;
}

class Otp extends BaseModel<Otp> {
  public refreshTokenId: FieldModel<number> = this.field("refresh_token_id", this.getRow);
  public otpType: FieldModel<string> = this.field("type", this.getRow);
  public value: FieldModel<string> = this.field("value", this.getRow);
  public mobileOtpValue: FieldModel<string> = this.field("mobile_otp_value", this.getRow);
  public userinfo: FieldModel<string> = this.field("userinfo", this.getRow);
  public createdAt: FieldModel<Date> = new FieldModel<Date>("created_at", this.getRow);
  public updatedAt: FieldModel<Date> = new FieldModel<Date>("updated_at", this.getRow);
  private userInfo: IInfo;

  constructor(row?: any) {
    super(row);
  }

  public clone(): Otp {
    return new Otp(this.getRow());
  }

  public getTableName(): string {
    return "t_otp";
  }

  public setUserInfo(userInfo: IInfo) {
    this.userInfo = userInfo;
    this.userinfo.set(JSON.stringify(this.userInfo));
  }

  public getUserInfo(): IInfo {
    if (!this.userInfo) {
      this.userInfo = JSON.parse(this.userinfo.get());
    }
    return this.userInfo;
  }
}

export default Otp;
