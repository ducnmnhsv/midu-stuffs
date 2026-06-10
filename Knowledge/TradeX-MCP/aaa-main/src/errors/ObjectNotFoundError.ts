import {Errors} from "tradex-common";

export const OBJECT_NOT_FOUND_ERROR_CODE = "OBJECT_NOT_FOUND";

export default class ObjectNotFoundError extends Errors.GeneralError {
  constructor(source?: Error) {
    super(OBJECT_NOT_FOUND_ERROR_CODE, undefined, source);
  }
}
