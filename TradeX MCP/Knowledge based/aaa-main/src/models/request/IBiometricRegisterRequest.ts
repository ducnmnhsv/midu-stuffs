import { Errors, Models, Utils } from "tradex-common";

const { validate } = Utils;

export interface IBiometricRegisterRequest extends Models.IDataRequest {
  password: string;
  publicKey: string;
  biometricType?: string;
  deviceId: string;
  macAddress?: string;
  platform?: string;
  osVersion?: string;
  appVersion?: string;
  sourceIp?: string;
  device_id?: string;
}

export function validateRegisterRequest(request: IBiometricRegisterRequest, throwable: Errors.InvalidParameterError) {
  validate(request.publicKey, "publicKey")
    .setRequire()
    .throwValid(throwable);
  validate(request.password, "password")
    .setRequire()
    .throwValid(throwable);
}

export interface IBiometricRegisterKisRequest extends Models.IDataRequest {
  wordMatrixId: number;
  wordMatrixValue: string;
  publicKey: string;
  kisToken: string;
  biometricType?: string;
  deviceId: string;
  macAddress?: string;
  platform?: string;
  osVersion?: string;
  appVersion?: string;
  sourceIp?: string;
  device_id?: string;
  rid?: string;
}

export function validateRegisterKisRequest(request: IBiometricRegisterKisRequest, throwable: Errors.InvalidParameterError) {
  validate(request.publicKey, "publicKey")
    .setRequire()
    .throwValid(throwable);
  validate(request.wordMatrixId, "wordMatrixId")
    .setRequire()
    .throwValid(throwable);
  validate(request.wordMatrixValue, "wordMatrixValue")
    .setRequire()
    .throwValid(throwable);
}
