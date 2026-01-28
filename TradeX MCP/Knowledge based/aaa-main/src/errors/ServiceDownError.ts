import {Errors} from "tradex-common";

class ServiceDownError extends Errors.GeneralError {
  constructor() {
    super("SERVICE_DOWN", null);
  }
}

export default ServiceDownError;
