import {DefaultBaseModel} from "./BaseModel";
import FieldModel from "./FieldModel";

class UsernameMapping extends DefaultBaseModel<UsernameMapping> {
  public id: FieldModel<number> = this.field("id", this.getRow);
  public username: FieldModel<string> = this.field("username", this.getRow);
  public accountNumber: FieldModel<string> = this.field("account_number", this.getRow);

  constructor(row?: any) {
    super(row);
  }

  public clone(): UsernameMapping {
    return new UsernameMapping(this.getRow());
  }

  public getTableName(): string {
    return "t_username_mapping";
  }
}

export default UsernameMapping;
