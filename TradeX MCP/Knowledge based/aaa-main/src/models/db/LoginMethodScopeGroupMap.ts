import FieldModel from "./FieldModel";
import {BaseModel} from "./BaseModel";

class LoginMethodScopeGroupMap extends BaseModel<LoginMethodScopeGroupMap> {
  public loginMethodId: FieldModel<number> = this.field("login_method_id", this.getRow);
  public groupId: FieldModel<number> = this.field("scope_group_id", this.getRow);

  constructor(row: any) {
    super(row);
  }

  public clone(): LoginMethodScopeGroupMap {
    return new LoginMethodScopeGroupMap(this.getRow());
  }

  public getTableName(): string {
    return "t_login_method_scope_group_map";
  }
}

export default LoginMethodScopeGroupMap;
