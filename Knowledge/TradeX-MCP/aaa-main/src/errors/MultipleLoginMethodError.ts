import {Errors} from "tradex-common";

class MultipleLoginMethodError extends Errors.GeneralError {
  constructor() {
    super("MULTIPLE_LOGIN_METHODS", null, null, null);
  }
}

export default MultipleLoginMethodError;
