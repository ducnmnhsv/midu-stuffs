import {Errors} from "tradex-common";

class NoLoginMethodError extends Errors.GeneralError {
  constructor() {
    super("NO_LOGIN_METHOD", null, null, null);
  }
}

export default NoLoginMethodError;
