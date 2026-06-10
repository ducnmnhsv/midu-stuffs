import {DefaultBaseModel} from "./BaseModel";
import FieldModel from "./FieldModel";
import {IUserInfo} from "../response/ILoginRes";
import conf from "../../conf";

class ServiceUser extends DefaultBaseModel<ServiceUser> {
  public id: FieldModel<number> = this.field("id", this.getRow);
  public username: FieldModel<string> = this.field("username", this.getRow);
  public registerMobileOtp: FieldModel<boolean> = this.field("register_mobile_otp", this.getRow);
  public avatar: FieldModel<string> = this.field("avatar", this.getRow);
  public email: FieldModel<string> = this.field("email", this.getRow);
  public phoneCode: FieldModel<string> = this.field("phone_code", this.getRow);
  public phoneNumber: FieldModel<string> = this.field("phone_number", this.getRow);
  public birthday: FieldModel<string> = this.field("birthday", this.getRow);

  constructor(row?: any) {
    super(row);
  }

  public clone(): ServiceUser {
    return new ServiceUser(this.getRow());
  }

  public getTableName(): string {
    return "t_service_user";
  }

  public toUserInfo(): IUserInfo {
    return {
      username: this.username.get(),
      id: this.id.get(),
      avatar: this.avatar.get() && conf.defaultAvatar,
      birthday: this.birthday.get(),
      email: this.email.get(),
      phoneCode: this.phoneCode.get(),
      phoneNumber: this.phoneNumber.get(),
    };
  }
}

export default ServiceUser;
