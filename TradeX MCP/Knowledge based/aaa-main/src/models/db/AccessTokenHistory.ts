import { BaseModel } from "./BaseModel";
import FieldModel from "./FieldModel";

export default class AccessTokenHistory extends BaseModel<AccessTokenHistory> {
  public id: FieldModel<number> = this.field("id", this.getRow);
  public refreshToken: FieldModel<string> = this.field("refresh_token", this.getRow);
  public userId: FieldModel<number> = this.field("user_id", this.getRow);
  public serviceUserId: FieldModel<number> = this.field("service_user_id", this.getRow);
  public sourceIp: FieldModel<string> = this.field("source_ip", this.getRow);
  public loginMethodId: FieldModel<number> = this.field("login_method_id", this.getRow);
  public deviceType: FieldModel<string> = this.field("device_type", this.getRow);
  public extendData: FieldModel<string> = this.field("extend_data", this.getRow);
  public clientId: FieldModel<number> = this.field("client_id", this.getRow);
  public parentId: FieldModel<number> = this.field("parent_id", this.getRow);
  public macAddress: FieldModel<string> = this.field("mac_address", this.getRow);
  public platform: FieldModel<string> = this.field("platform", this.getRow);
  public osVersion: FieldModel<string> = this.field("os_version", this.getRow);
  public appVersion: FieldModel<string> = this.field("app_version", this.getRow);
  public createdAt: FieldModel<Date> = new FieldModel<Date>("created_at", this.getRow);
  
  constructor(row?: any) {
    super(row);
  }

  public getTableName(): string {
    return "t_access_token_history";
  }
  
  public clone(): AccessTokenHistory {
    return new AccessTokenHistory(this.getRow());
  }
}