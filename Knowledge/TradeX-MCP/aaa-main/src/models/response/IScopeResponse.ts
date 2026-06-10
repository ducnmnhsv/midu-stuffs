import {Models} from "tradex-common";
import IEditor from "./IEditor";

export default interface IScopeResponse {
  id: number;
  name: string;
  uriPattern: string;
  forwardType: string;
  forwardData: Models.AAA.ICommonForward;
  createdBy: IEditor;
  createdAt: string;
  updatedBy: IEditor;
  updatedAt: string;
  scopeGroupIds: number[];
}