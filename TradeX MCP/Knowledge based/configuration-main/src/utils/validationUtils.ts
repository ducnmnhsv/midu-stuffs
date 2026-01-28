import { Utils } from "tradex-common";
import { ERROR_CODES } from "../constants/defaultRequestParameter";

export function validateDomain(data: string, invalidParameters: any) {
  Utils.validate(data, "domain")
    .add((value: string, name: string) => {
      if (typeof value === "string") {
        return null;
      } else {
        return Utils.createFailValidation(ERROR_CODES.INVALID_VALUE, [], name);
      }
    })
    .setRequire()
    .throwValid(invalidParameters);
}

export function validateFetchCount(data: number, invalidParameters: any) {
  Utils.validate(data, "fetchCount")
    .add((value: number, name: string) => {
      if (value <= 100) {
        return null;
      } else {
        return Utils.createFailValidation(
          ERROR_CODES.EXCEED_MAXIMUM,
          ["100"],
          name,
        );
      }
    })
    .throwValid(invalidParameters);
}

export function validateLastSequence(data: number, invalidParameters: any) {
  Utils.validate(data, "lastSequence")
    .add((value: number, name: string) => {
      if (typeof value === "number") {
        return null;
      } else {
        return Utils.createFailValidation(ERROR_CODES.INVALID_VALUE, [], name);
      }
    })
    .throwValid(invalidParameters);
}

export function validateIsFullData(data: boolean, invalidParameters: any) {
  Utils.validate(data, "isFullData")
    .add((value: boolean, name: string) => {
      if (value === true || value === false) {
        return null;
      } else {
        return Utils.createFailValidation(ERROR_CODES.INVALID_VALUE, [], name);
      }
    })
    .throwValid(invalidParameters);
}

export function validateId(data: number, invalidParameters: any) {
  Utils.validate(data, "id")
    .add((value: number, name: string) => {
      if (typeof value === "number") {
        return null;
      } else {
        return Utils.createFailValidation(ERROR_CODES.INVALID_VALUE, [], name);
      }
    })
    .setRequire()
    .throwValid(invalidParameters);
}
