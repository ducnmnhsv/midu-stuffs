import {Errors} from "tradex-common";

class UnAuthenticationError extends Errors.GeneralError {
  constructor() {
    super("UN_AUTHENTICATION", null, null, null);
  }
}

export default UnAuthenticationError;
