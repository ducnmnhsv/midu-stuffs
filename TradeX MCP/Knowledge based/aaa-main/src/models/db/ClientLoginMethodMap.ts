import FieldModel from "./FieldModel";
import {BaseModel} from "./BaseModel";

class ClientLoginMethodMap extends BaseModel<ClientLoginMethodMap> {
  public clientId: FieldModel<number> = this.field("client_id", this.getRow);
  public loginMethodId: FieldModel<number> = this.field("login_method_id", this.getRow);

  constructor(row?: any) {
    super(row);
  }

  public clone(): ClientLoginMethodMap {
    return new ClientLoginMethodMap(this.getRow());
  }

  public getTableName(): string {
    return "t_client_login_method_map";
  }
}

export default ClientLoginMethodMap;
