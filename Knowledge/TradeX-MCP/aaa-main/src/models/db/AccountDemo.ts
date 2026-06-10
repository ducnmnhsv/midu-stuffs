import FieldModel from "./FieldModel";
import {BaseModel} from "./BaseModel";

class AccountDemo extends BaseModel<AccountDemo> {
  public id: FieldModel<number> = this.field("id", this.getRow);
  public username: FieldModel<string> = this.field("username", this.getRow);
  public password: FieldModel<string> = this.field("password", this.getRow);
  public realUsername: FieldModel<string> = this.field("real_username", this.getRow);
  public realPassword: FieldModel<string> = this.field("real_password", this.getRow);
  public domain: FieldModel<string> = this.field("domain", this.getRow);
  public description: FieldModel<string> = this.field("description", this.getRow);
  public loginResponse: FieldModel<string> = this.field("login_response", this.getRow);
  public createdAt: FieldModel<Date> = new FieldModel<Date>("created_at", this.getRow);
  public updatedAt: FieldModel<Date> = new FieldModel<Date>("updated_at", this.getRow);

  constructor(row?: any) {
    super(row);
  }

  public clone(): AccountDemo {
    return new AccountDemo(this.getRow());
  }

  public getTableName(): string {
    return "t_account_demo";
  }
}

export default AccountDemo;
