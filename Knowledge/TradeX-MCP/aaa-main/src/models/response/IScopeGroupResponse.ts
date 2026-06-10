import IEditor from "./IEditor";

export default interface IScopeGroupResponse {
  id: number;
  scopeGroupName: string;
  createdBy: IEditor;
  createdAt: string;
  updatedBy: IEditor;
  updatedAt: string;
}