import {Errors} from "tradex-common";

class NoGrantTypeError extends Errors.GeneralError {
  constructor() {
    super("NO_GRANT_TYPE", null, null, null);
  }
}

export default NoGrantTypeError;
