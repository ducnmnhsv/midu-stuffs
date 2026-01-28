import { Errors, Utils } from "tradex-common";

export interface ILinkAccountConfirmRequest {
  partnerId: string;
  authCode: string;
  partnerUsername: string;
  userId: number;
  username: string;
  sign: string;
}

export function validateLinkAccountConfirmRequest(request: ILinkAccountConfirmRequest, throwable: Errors.InvalidParameterError) {
    Utils.validate(request.partnerId, "partnerId")
    .setRequire()
    .throwValid(throwable);
    Utils.validate(request.authCode, "authCode")
    .setRequire()
    .throwValid(throwable);
    Utils.validate(request.partnerUsername, "partnerUsername")
    .setRequire()
    .throwValid(throwable);
    Utils.validate(request.username, "username")
    .setRequire()
    .throwValid(throwable);
    Utils.validate(request.sign, "sign")
    .setRequire()
    .throwValid(throwable);
}
