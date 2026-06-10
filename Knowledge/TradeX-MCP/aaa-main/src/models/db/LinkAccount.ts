import {DefaultBaseModel} from "./BaseModel";
import FieldModel from "./FieldModel";

class LinkAccount extends DefaultBaseModel<LinkAccount> {
  public id: FieldModel<number> = this.field("id", this.getRow);
  // partnerId in this system
  public partnerId: FieldModel<string> = this.field("partner_id", this.getRow);
  public partnerUserId: FieldModel<number> = this.field("partner_user_id", this.getRow);
  public partnerUsername: FieldModel<string> = this.field("partner_username", this.getRow);
  public userId: FieldModel<number> = this.field("user_id", this.getRow);
  public username: FieldModel<string> = this.field("username", this.getRow);
  public joinLeaderboard: FieldModel<boolean> = this.field("join_leaderboard", this.getRow);
  public subAccount: FieldModel<string> = this.field("sub_account", this.getRow);
  public initRequest: FieldModel<string> = this.field("init_request", this.getRow);
  public confirmRequest: FieldModel<string> = this.field("confirm_request", this.getRow);
  public infoAccessGranted: FieldModel<boolean> = this.field("info_access_granted", this.getRow);
  public partnerFullname : FieldModel<string> = this.field("partner_fullname", this.getRow);

  constructor(row: any) {
    super(row);
  }

  public clone(): LinkAccount {
    return new LinkAccount(this.getRow());
  }

  public getTableName(): string {
    return "t_link_account";
  }
}

export default LinkAccount;
