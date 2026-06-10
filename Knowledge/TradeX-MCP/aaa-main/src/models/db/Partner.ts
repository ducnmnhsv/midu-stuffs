import {DefaultBaseModel} from "./BaseModel";
import FieldModel from "./FieldModel";

class Partner extends DefaultBaseModel<Partner> {
  public id: FieldModel<string> = this.field("id", this.getRow);
  public targetPartnerId: FieldModel<string> = this.field("partner_id", this.getRow);
  public name: FieldModel<string> = this.field("name", this.getRow);
  public loginUrl: FieldModel<string> = this.field("login_url", this.getRow);
  public loginClientId: FieldModel<string> = this.field("login_client_id", this.getRow);
  public loginClientSecret: FieldModel<string> = this.field("login_client_secret", this.getRow);
  public confirmLinkUrl: FieldModel<string> = this.field("confirm_link_url", this.getRow);
  public unlinkUrl: FieldModel<string> = this.field("unlink_url", this.getRow);
  public initLinkUrl: FieldModel<string> = this.field("init_link_url", this.getRow);
  public integrationType: FieldModel<string> = this.field("type", this.getRow);
  public privateKey: FieldModel<string> = this.field("private_key", this.getRow);
  public publicKey: FieldModel<string> = this.field("public_key", this.getRow);
  public description: FieldModel<string> = this.field("description", this.getRow);
  public iconUrl: FieldModel<string> = this.field("icon_url", this.getRow);
  public notyOtpUrl : FieldModel<string> = this.field("noty_otp_url", this.getRow);


  constructor(row: any) {
    super(row);
  }

  public clone(): Partner {
    return new Partner(this.getRow());
  }

  public getTableName(): string {
    return "t_partner";
  }
}

export default Partner;
