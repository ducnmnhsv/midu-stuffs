import {DefaultBaseModel} from "./BaseModel";
import FieldModel from "./FieldModel";

class LinkAccountDraft extends DefaultBaseModel<LinkAccountDraft> {
  public id: FieldModel<number> = this.field("id", this.getRow);
  // public clientId: FieldModel<number> = this.field("client_id", this.getRow);
  public partnerId: FieldModel<string> = this.field("partner_id", this.getRow);
  public partnerUsername: FieldModel<string> = this.field("partner_username", this.getRow);
  public partnerUserId: FieldModel<number> = this.field("partner_user_id", this.getRow);
  public username: FieldModel<string> = this.field("username", this.getRow);
  public authCode: FieldModel<string> = this.field("auth_code", this.getRow);
  public expirationAt: FieldModel<Date> = this.field("expiration_at", this.getRow);
  public request: FieldModel<string> = this.field("request", this.getRow);

  constructor(row: any) {
    super(row);
  }

  public clone(): LinkAccountDraft {
    return new LinkAccountDraft(this.getRow());
  }

  public getTableName(): string {
    return "t_link_account_draft";
  }
}

export default LinkAccountDraft;
