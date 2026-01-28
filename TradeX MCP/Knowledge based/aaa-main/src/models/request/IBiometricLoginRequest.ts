import { Errors, Models, Utils } from "tradex-common";

const { validate } = Utils;
const { InvalidParameterError } = Errors;
const invalidParams = new InvalidParameterError();

export interface IBiometricLoginRequest extends Models.IDataRequest {
  signatureValue: string;
  username: string;
  password?: string;
  sec_code?: string;
  grant_type: string;
  client_id: string;
  client_secret: string;
  macAddress?: string;
  platform?: string;
  osVersion?: string;
  appVersion?: string;
  sourceIp?: string;
  device_id?: string;
  headers?: any;
  session_time_in_minute?: number; //allow users to choose login in a short period
  sessionTimeInMinute?: number; //allow users to choose login in a short period
}

export function validateLoginRequest(request: IBiometricLoginRequest) {
  validate(request.sec_code, "sec_code")
    .setRequire()
    .throwValid(invalidParams);
  validate(request.username, "username")
    .setRequire()
    .throwValid(invalidParams);
}
