import FieldModel from "./FieldModel";
import {DefaultBaseModel} from "./BaseModel";

/**
 * create table t_login_method_step (     `id` BIGINT NOT NULL,     `login_method_id` INT not null,     `step` INT not null,     `name` varchar(128),     `desc` varchar(512),     PRIMARY KEY(id),     FOREIGN KEY (login_method_id) REFERENCES t_login_method(id) ) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_bin;
 */
class LoginMethodStep extends DefaultBaseModel<LoginMethodStep> {
  public id: FieldModel<number> = this.field("id", this.getRow);
  public loginMethodId: FieldModel<number> = this.field("login_method_id", this.getRow);
  public step: FieldModel<number> = this.field("step", this.getRow);
  public name: FieldModel<string> = this.field("name", this.getRow);
  public desc: FieldModel<string> = this.field("desc", this.getRow);

  constructor(row?: any) {
    super(row);
  }

  public clone(): LoginMethodStep {
    return new LoginMethodStep(this.getRow());
  }

  public getTableName(): string {
    return "t_login_method_step";
  }
}

export default LoginMethodStep;
