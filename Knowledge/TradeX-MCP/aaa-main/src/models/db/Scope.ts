import {Models} from "tradex-common";
import {DefaultBaseModel} from "./BaseModel";
import FieldModel from "./FieldModel";

class Scope extends DefaultBaseModel<Scope> {
  public id: FieldModel<number> = this.field("id", this.getRow);
  public name: FieldModel<string> = this.field("name", this.getRow);
  public uriPattern: FieldModel<string> = this.field("uri_pattern", this.getRow);
  public forwardType: FieldModel<string> = this.field("forward_type", this.getRow);
  public forwardDataJson: FieldModel<string> = this.field("forward_data", this.getRow);
  private forwardData: Models.AAA.ICommonForward;
  private scopeResponse: Models.AAA.IScope;

  constructor(row: any) {
    super(row);
  }

  public hasForwardData() {
    const s: string = this.forwardDataJson.get();
    return s && s.length > 0;
  }

  public getForwardData(): Models.AAA.ICommonForward {
    if (!this.forwardData) {
      this.forwardData = JSON.parse(this.forwardDataJson.get());
    }
    return this.forwardData;
  }

  public setForwardData(forwardData: Models.AAA.ICommonForward) {
    this.forwardData = forwardData;
    this.forwardDataJson.set(JSON.stringify(this.forwardData));
  }

  public clone(): Scope {
    return new Scope(this.getRow());
  }

  public getTableName(): string {
    return "t_scope";
  }

  public getScopeResponse(): Models.AAA.IScope {
    if (!this.scopeResponse) {
      this.scopeResponse = {
        id: this.id.get(),
        name: this.name.get(),
        uriPattern: this.uriPattern.get(),
        forwardType: this.forwardType.get(),
        forwardData: this.getForwardData(),
      };
      if (!this.scopeResponse.forwardData.forwardType) {
        this.scopeResponse.forwardData.forwardType = this.scopeResponse.forwardType;
      }
    }
    return this.scopeResponse;
  }
}

export default Scope;
