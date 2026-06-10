import { Errors, Utils } from "tradex-common";

export interface ILinkAccountUnlinkRequest {
  partnerId: string;
  username: string;
  userId?: number;
  sign: string;
}

export function validateLinkAccountConfirmRequest(request: ILinkAccountUnlinkRequest, throwable: Errors.InvalidParameterError) {
    Utils.validate(request.partnerId, "partnerId")
    .setRequire()
    .throwValid(throwable);
    Utils.validate(request.username, "username")
    .setRequire()
    .throwValid(throwable);
    Utils.validate(request.sign, "sign")
    .setRequire()
    .throwValid(throwable);
}
