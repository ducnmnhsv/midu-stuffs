import {DefaultBaseModel} from "./BaseModel";
import FieldModel from "./FieldModel";

class Client extends DefaultBaseModel<Client> {
  public id: FieldModel<number> = this.field("id", this.getRow);
  public userId: FieldModel<number> = this.field("user_id", this.getRow);
  public clientId: FieldModel<string> = this.field("client_id", this.getRow);
  public clientSecret: FieldModel<string> = this.field("client_secret", this.getRow);
  public desciption: FieldModel<string> = this.field("desciption", this.getRow);
  public status: FieldModel<number> = this.field("status", this.getRow);
  public appVersion: FieldModel<string> = this.field("app_version", this.getRow);
  constructor(row?: any) {
    super(row);
  }

  public clone(): Client {
    return new Client(this.getRow());
  }

  public getTableName(): string {
    return "t_client";
  }
}

export default Client;
