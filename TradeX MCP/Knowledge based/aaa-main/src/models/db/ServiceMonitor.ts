import {BaseModel} from "./BaseModel";
import FieldModel from "./FieldModel";

class ServiceMonitor extends BaseModel<ServiceMonitor> {
  public serviceName: FieldModel<string> = this.field("service_name", this.getRow);
  public nodeId: FieldModel<string> = this.field("node_id", this.getRow);
  public updatedTime: FieldModel<string> = this.field("updated_time", this.getRow);

  constructor(row: any) {
    super(row);
  }

  public getTableName(): string {
    return "t_service_monitor";
  }

  public clone(): ServiceMonitor {
    return new ServiceMonitor(this.getRow());
  }
}

export default ServiceMonitor;
