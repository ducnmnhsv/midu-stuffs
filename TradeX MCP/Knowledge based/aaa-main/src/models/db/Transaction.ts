import {BaseModel} from "./BaseModel";
import FieldModel from "./FieldModel";

interface ITransaction {
  id?: number;
  user_id: number;
  scope_id: number;
  service_user_id: number;
  client_id: number;
  refresh_token_id: number;
  transaction_id: string;
  data: string;
  to_topic: string;
  to_uri: string;
  created_at?: Date;
}

class Transaction extends BaseModel<Transaction> {
  public id: FieldModel<number> = this.field("id", this.getRow);
  public userId: FieldModel<number> = this.field("user_id", this.getRow);
  public scopeId: FieldModel<number> = this.field("scope_id", this.getRow);
  public serviceUserId: FieldModel<number> = this.field("service_user_id", this.getRow);
  public clientId: FieldModel<number> = this.field("client_id", this.getRow);
  public refreshTokenId: FieldModel<number> = this.field("refresh_token_id", this.getRow);
  public transactionId: FieldModel<string> = this.field("transaction_id", this.getRow);
  public data: FieldModel<string> = this.field("data", this.getRow);
  public toTopic: FieldModel<string> = this.field("to_topic", this.getRow);
  public toUri: FieldModel<string> = this.field("to_uri", this.getRow);
  public createdAt: FieldModel<Date> = this.field("created_at", this.getRow, new Date());

  constructor(row?: any) {
    super(row);
  }

  public clone(): Transaction {
    return new Transaction(this.getRow());
  }

  public getTableName(): string {
    return "t_transaction";
  }
}

export default Transaction;

export {ITransaction};
