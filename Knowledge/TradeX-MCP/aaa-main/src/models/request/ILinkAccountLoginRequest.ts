import { Errors, Models, Utils } from "tradex-common";

export interface ILinkAccountLoginRequest extends Models.IDataRequest {
  partnerId: string;
  rid?: string;
  infoAccessGranted?: boolean;
  session_time_in_minute?: number;
  sessionTimeInMinute?: number;
}

export function validateLinkAccountLoginRequest(request: ILinkAccountLoginRequest, throwable: Errors.InvalidParameterError) {
    Utils.validate(request.partnerId, "partnerId")
    .setRequire()
    .throwValid(throwable);
}
