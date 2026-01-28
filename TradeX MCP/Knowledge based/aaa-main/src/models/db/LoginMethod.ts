import FieldModel from "./FieldModel";
import {DefaultBaseModel} from "./BaseModel";

class LoginMethod extends DefaultBaseModel<LoginMethod> {
  public id: FieldModel<number> = this.field("id", this.getRow);
  public serviceCode: FieldModel<string> = this.field("service_code", this.getRow);
  public grantType: FieldModel<string> = this.field("grant_type", this.getRow);
  public accessTokenTtl: FieldModel<number> = this.field("access_token_ttl", this.getRow);
  public refreshTokenTtl: FieldModel<number> = this.field("refresh_token_ttl", this.getRow);
  public refreshTokenLongTtl: FieldModel<number> = this.field("refresh_token_long_ttl", this.getRow);
  public multiFactorTtl: FieldModel<number> = this.field("multi_factor_ttl", this.getRow);
  public publicScopes: FieldModel<string> = this.field("public_scopes", this.getRow);
  public msName: FieldModel<string> = this.field("ms_name", this.getRow);
  public msUri: FieldModel<string> = this.field("ms_uri", this.getRow);
  public isDefault: FieldModel<number> = this.field("is_default", this.getRow);
  public extraData: FieldModel<string> = this.field("extra_data", this.getRow);

  constructor(row?: any) {
    super(row);
  }

  public clone(): LoginMethod {
    return new LoginMethod(this.getRow());
  }

  public getTableName(): string {
    return "t_login_method";
  }
}

export default LoginMethod;
