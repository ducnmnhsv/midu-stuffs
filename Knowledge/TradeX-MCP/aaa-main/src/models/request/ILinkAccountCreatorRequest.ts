import { Errors, Models, Utils } from "tradex-common";

export interface ILinkAccountCreatorRequest extends Models.IDataRequest {
  partnerId: string;
  authCode: string;
  partnerUsername: string;
  optBoard: boolean;
  subAccount: string;
  rid?: string;
  infoAccessGranted?: boolean;
}

export function validateLinkAccountConfirmRequest(request: ILinkAccountCreatorRequest, throwable: Errors.InvalidParameterError) {
    Utils.validate(request.partnerId, "partnerId")
    .setRequire()
    .throwValid(throwable);
    Utils.validate(request.authCode, "authCode")
    .setRequire()
    .throwValid(throwable);
    Utils.validate(request.partnerUsername, "partnerUsername")
    .setRequire()
    .throwValid(throwable);
    Utils.validate(request.optBoard, "optBoard")
    .setRequire()
    .throwValid(throwable);
}
