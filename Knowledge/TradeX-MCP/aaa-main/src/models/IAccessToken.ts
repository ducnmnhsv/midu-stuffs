import {Models} from "tradex-common";
import {IExtendData} from "./db/RefreshToken";

type IConnectionIdentifier = Models.IConnectionIdentifier;

declare interface IAccessToken extends IExtendData {
  dm?: string;
  uId?: number;
  cId: number;
  suId?: number;
  lm: number;
  rId: number;
  sc?: string;
  rls?: string[];
  step?: number, // if login with n factor. and step is not null then step define that login is not yet completed. step have value from 1 to n-1. when completed. step is removed
  pl?: string; // platform
  gt?: string; // grantType
  osV?: string; // osVersion
  appV?: string; // appVersion
  madr?: string; // macAddress
}

export {
  IAccessToken,
  IConnectionIdentifier,
};
