import { DefaultBaseModel } from "./BaseModel";
import FieldModel from "./FieldModel";

class Biometric extends DefaultBaseModel<Biometric> {
  public id: FieldModel<number> = this.field("id", this.getRow);
  public userId: FieldModel<number> = this.field("user_id", this.getRow);
  public password: FieldModel<string> = this.field("password", this.getRow);
  public publicKey: FieldModel<string> = this.field("public_key", this.getRow);
  public username: FieldModel<string> = this.field("username", this.getRow);
  public grantType: FieldModel<string> = this.field("grant_type", this.getRow);
  public clientId: FieldModel<number> = this.field("client_id", this.getRow);
  public platform: FieldModel<string> = this.field("platform", this.getRow);
  public osVersion: FieldModel<string> = this.field("os_version", this.getRow);
  public appVersion: FieldModel<string> = this.field("app_version", this.getRow);
  public deviceId: FieldModel<string> = this.field("device_id", this.getRow);
  public sourceIp: FieldModel<string> = this.field("source_ip", this.getRow);
  public isDeleted: FieldModel<boolean> = this.field("is_deleted", this.getRow);
  public deleteReason: FieldModel<string> = this.field("delete_reason", this.getRow);
  public createdAt: FieldModel<Date> = this.field("created_at", this.getRow);
  public updatedAt: FieldModel<Date> = this.field("updated_at", this.getRow);
  public clientSecret: FieldModel<string> = this.field("client_secret", this.getRow);
  public biometricType: FieldModel<string> = this.field("biometric_type", this.getRow);
  public otpIndex: FieldModel<number | string> = this.field("otp_index", this.getRow);
  public otpValue: FieldModel<string> = this.field("otp_value", this.getRow);
  public status: FieldModel<string> = this.field("status", this.getRow);

  constructor(row?: any) {
    super(row);
  }

  public clone(): Biometric {
    return new Biometric(this.getRow());
  }

  public getTableName(): string {
    return "t_biometric";
  }
}

export default Biometric;
