import { Errors, Models, Utils } from "tradex-common";

export interface ILinkAccountDeleteRequest extends Models.IDataRequest {
  partnerId: string;
  rid?: string;
}

export function validateLinkAccountConfirmRequest(request: ILinkAccountDeleteRequest, throwable: Errors.InvalidParameterError) {
    Utils.validate(request.partnerId, "partnerId")
    .setRequire()
    .throwValid(throwable);
}
