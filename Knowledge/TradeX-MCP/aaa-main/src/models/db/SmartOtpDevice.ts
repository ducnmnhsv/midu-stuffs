import { DefaultBaseModel } from "./BaseModel";
import FieldModel from "./FieldModel";

class SmartOtpDevice extends DefaultBaseModel<SmartOtpDevice> {
  public id: FieldModel<number> = this.field("id", this.getRow);
  public username: FieldModel<string> = this.field("username", this.getRow);
  public deviceUniqueId: FieldModel<string> = this.field("device_unique_id", this.getRow);
  public status: FieldModel<number> = this.field("status", this.getRow);
  public createdAt: FieldModel<Date> = this.field("created_at", this.getRow);
  public updatedAt: FieldModel<Date> = this.field("updated_at", this.getRow);

  constructor(row?: any) { super(row); }
  public clone(): SmartOtpDevice { return new SmartOtpDevice(this.getRow()); }
  public getTableName(): string { return "t_sotp_device"; }
}

export default SmartOtpDevice;