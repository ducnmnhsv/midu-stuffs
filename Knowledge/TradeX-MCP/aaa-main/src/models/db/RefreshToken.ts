import {BaseModel} from "./BaseModel";
import FieldModel from "./FieldModel";
import {IConnectionIdentifier} from "../IAccessToken";
import {Models} from "tradex-common";

declare interface IExtendData {
  sgIds: number[];
  conId?: IConnectionIdentifier;
  sc?: string;
  su?: string;
  ud?: Models.IUserData; // userdata
  pl?: string;
  gt?: string;
  osV?: string;
  appV?: string;
  rTtl?: number;
  aTtl?: number;
  sId?: string; // sessionId for lotte-rest-bridge
}

function extractExtendData(data: IExtendData): IExtendData {
  return {
    sgIds: data.sgIds,
    conId: data.conId,
    sc: data.sc,
    su: data.su,
    ud: data.ud,
    pl: data.pl,
    gt: data.gt,
    osV: data.osV,
    appV: data.appV,
    rTtl: data.rTtl,
    aTtl: data.aTtl,
    sId: data.sId,
  };
}

class RefreshToken extends BaseModel<RefreshToken> {
  public id: FieldModel<number> = this.field("id", this.getRow);
  public clientId: FieldModel<number> = this.field("client_id", this.getRow);
  public userId: FieldModel<number> = this.field("user_id", this.getRow);
  public serviceUserId: FieldModel<number> = this.field("service_user_id", this.getRow);
  public loginMethodId: FieldModel<number> = this.field("login_method_id", this.getRow);
  public token: FieldModel<string> = this.field("token", this.getRow);
  public updatedTime: FieldModel<string> = this.field("updated_at", this.getRow);
  public extendData: FieldModel<string> = this.field("extend_data", this.getRow);
  public sourceIp: FieldModel<string> = this.field("source_ip", this.getRow);
  public deviceType: FieldModel<string> = this.field("device_type", this.getRow);
  public expiredAt: FieldModel<Date> = this.field("expired_at", this.getRow);
  public parentId: FieldModel<number> = this.field("parent_id", this.getRow);
  public macAddress: FieldModel<string> = this.field("mac_address", this.getRow);
  public platform: FieldModel<string> = this.field("platform", this.getRow);
  public osVersion: FieldModel<string> = this.field("os_version", this.getRow);
  public appVersion: FieldModel<string> = this.field("app_version", this.getRow);
  private extData: IExtendData = null;

  constructor(row?: any) {
    super(row);
  }

  public setExtendData(data: IExtendData): void {
    this.extData = data;
    this.extendData.set(JSON.stringify(data));
  }

  public getExtendData(): IExtendData {
    if (!this.extData) {
      this.extData = JSON.parse(this.extendData.get());
    }
    return this.extData;
  }

  public getTableName(): string {
    return "t_refresh_token";
  }

  public clone(): RefreshToken {
    return new RefreshToken(this.getRow());
  }

  public isExpired(): boolean {
    return this.expiredAt.get().getTime() < new Date().getTime();
  }
}

export default RefreshToken;
export {
  IExtendData,
  extractExtendData,
};
