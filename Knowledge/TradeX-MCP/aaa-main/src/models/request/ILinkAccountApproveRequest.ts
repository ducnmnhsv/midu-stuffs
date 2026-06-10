import { Errors, Utils } from "tradex-common";

export interface ILinkAccountApproveRequest {
  partnerId: string;
  partnerUserId: number;
  partnerUsername: string;
  username: string;
  userId: number;
  joinLeaderboard: boolean;
  subAccount: string;
  infoAccessGranted: boolean;
}

export function validateLinkAccountApproveRequest(request: ILinkAccountApproveRequest, throwable: Errors.InvalidParameterError) {
    Utils.validate(request.partnerId, "partnerId")
    .setRequire()
    .throwValid(throwable);
    Utils.validate(request.partnerUsername, "partnerUsername")
    .setRequire()
    .throwValid(throwable);
    Utils.validate(request.username, "username")
    .setRequire()
    .throwValid(throwable);
    Utils.validate(request.userId, "userId")
    .setRequire()
    .throwValid(throwable);
}
