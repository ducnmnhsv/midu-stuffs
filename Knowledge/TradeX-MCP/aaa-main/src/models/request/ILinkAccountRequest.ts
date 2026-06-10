import { Errors, Models, Utils } from "tradex-common";

export interface ILinkAccountRequest extends Models.IDataRequest {
  partnerId: string;
  partnerUserId?: number;
  partnerUsername: string;
}

export function validateLinkAccountRequest(request: ILinkAccountRequest, throwable: Errors.InvalidParameterError) {
    Utils.validate(request.partnerId, "partnerId")
    .setRequire()
    .throwValid(throwable);
    Utils.validate(request.partnerUsername, "partnerUsername")
    .setRequire()
    .throwValid(throwable);
}