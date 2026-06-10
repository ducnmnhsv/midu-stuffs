import {DefaultBaseModel} from "./BaseModel";
import FieldModel from "./FieldModel";

class ScopeGroup extends DefaultBaseModel<ScopeGroup> {
  public id: FieldModel<number> = this.field("id", this.getRow);
  public parentId: FieldModel<number> = this.field("parent_id", this.getRow);
  public name: FieldModel<string> = this.field("scope_group_name", this.getRow);

  constructor(row?: any) {
    super(row);
  }

  public clone(): ScopeGroup {
    return new ScopeGroup(this.getRow());
  }

  public getTableName(): string {
    return "t_scope_group";
  }
}

export default ScopeGroup;
