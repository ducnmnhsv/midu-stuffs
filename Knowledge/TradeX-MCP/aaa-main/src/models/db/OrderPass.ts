import FieldModel from "./FieldModel";
import {BaseModel} from "./BaseModel";

class OrderPass extends BaseModel<OrderPass> {
  public id: FieldModel<number> = this.field("id", this.getRow);
  public secCode: FieldModel<string> = this.field("sec_code", this.getRow);
  public accountNo: FieldModel<string> = this.field("account_no", this.getRow);
  public subNo: FieldModel<string> = this.field("sub_no", this.getRow);
  public passType: FieldModel<string> = this.field("type", this.getRow);
  public password: FieldModel<string> = this.field("password", this.getRow);
  public createdAt: FieldModel<Date> = new FieldModel<Date>("created_at", this.getRow);
  public updatedAt: FieldModel<Date> = new FieldModel<Date>("updated_at", this.getRow);

  constructor(row?: any) {
    super(row);
  }

  public clone(): OrderPass {
    return new OrderPass(this.getRow());
  }

  public getTableName(): string {
    return "t_order_pass";
  }
}

export default OrderPass;
