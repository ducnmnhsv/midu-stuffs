import FieldModel from "./FieldModel";
import {BaseModel} from "./BaseModel";

/**
 * create table t_login_method_step_scope_group_map (     step_id BIGINT not null,     scope_group_id BIGINT not null,     primary key (step_id, scope_group_id),     FOREIGN KEY (step_id)         REFERENCES t_login_method_step(id));
 */

class LoginMethodStepScopeGroupMap extends BaseModel<LoginMethodStepScopeGroupMap> {
  public stepId: FieldModel<number> = this.field("step_id", this.getRow);
  public groupId: FieldModel<number> = this.field("scope_group_id", this.getRow);

  constructor(row: any) {
    super(row);
  }

  public clone(): LoginMethodStepScopeGroupMap {
    return new LoginMethodStepScopeGroupMap(this.getRow());
  }

  public getTableName(): string {
    return "t_login_method_step_scope_group_map";
  }
}

export default LoginMethodStepScopeGroupMap;
