import FieldModel from "./FieldModel";
import {BaseModel} from "./BaseModel";

class ScopeScopeGroupMap extends BaseModel<ScopeScopeGroupMap> {
  public scopeId: FieldModel<number> = this.field("scope_id", this.getRow);
  public groupId: FieldModel<number> = this.field("scope_group_id", this.getRow);

  constructor(row?: any) {
    super(row ? row : {});
  }

  public clone(): ScopeScopeGroupMap {
    return new ScopeScopeGroupMap(this.getRow());
  }

  public getTableName(): string {
    return "t_scope_scope_group_map";
  }
}

export default ScopeScopeGroupMap;
