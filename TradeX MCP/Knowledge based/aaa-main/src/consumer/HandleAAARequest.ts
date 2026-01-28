import { authenticate } from "../services/authenticationService";
import { handleMessage } from "./HandleForwardRequest";
import { Kafka, Models } from "tradex-common";
import { refreshAccessToken, revokeToken } from "../services/TokenService";
import { verifyOtp } from "../services/VerifyOtp";
import * as linkAccountService from "../services/linkAccountService";
import { scopeService } from "../services/ScopeService";
import { generateSaveOtpToken, notifyMobileBiometricOtp, notifyMobileOtp, notifyMobileOtpKisTtl, notifyMobileOtpNhsv } from "../services/otpService";
import Scope from "../models/db/Scope";
import {
  registerMobileOtp,
  unregisterMobileOtp,
  updateProfile
} from '../services/UserService';
import {
  importDbJsonService
} from '../services/ImportDbJsonService';
import {
  registerBiometric,
  cancelBiometricRegister,
  verifyBiometricOTP,
  queryBiometricStatus,
  verifyPwdBiometric,
  registerBiometricKis
} from "../services/BiometricService";
import {
  changeClientSecret, updateAppVersion
} from "../services/ClientService";
import { loginPartnerCredential } from "../services/loginPartnerCredentialService";
import conf from "../conf";
import { loginVerifyOtp } from "../services/authen/loginOtp";

// tslint:disable-next-line:cyclomatic-complexity
function handleAAAMessage(msg: Kafka.IMessage): Promise<any> | boolean {
  if (msg.uri === "post:/api/v1/login"
    || msg.uri === "post:/api/v1/login/sec"
    || msg.uri === "post:/api/v1/login/domain"
    || msg.uri === "post:/api/v1/login/social"
    || msg.uri === "post:/api/v1/loginCA"
    || msg.uri === "post:/api/v1/login/biometric"
    || msg.uri === "post:/api/v1/user/SocialLogin"
    || msg.uri === "post:/api/v1/login/organization"
    || msg.uri === "post:/api/v1/login/social/organization"
  ) {
    return authenticate(msg.data, msg);
  } else if (msg.uri === "post:/api/v1/login/partnerCredential") {
    return loginPartnerCredential(msg.data, msg);
  } else if (msg.uri === "post:/api/v1/refreshToken") {
    return refreshAccessToken(msg.data);
  } else if (msg.uri === "post:/api/v1/revokeToken") {
    return revokeToken(msg.data);
  } else if (msg.uri === "put:/api/v1/client/{id}/changeSecret") {
    return changeClientSecret(msg.data);
  } else if (msg.uri === "post:/api/v1/login/sec/verifyOTP") {
    if (conf.enableHandleOtp) {
      return verifyOtp(msg.data, msg);
    }
    return loginVerifyOtp(msg.data, msg);
  } else if (msg.uri === "get:/api/v1/partners") {
    return linkAccountService.findAllPartners();
  } else if (msg.uri === "get:/api/v1/linkAccounts") {
    return linkAccountService.findAllLinkAccount(msg.data);
  } else if (msg.uri === "post:/api/v1/linkAccounts") {
    return linkAccountService.createLinkAccount(msg.data, `${msg.transactionId}`);
  } else if (msg.uri === "delete:/api/v1/linkAccounts/{partnerId}") {
    return linkAccountService.deleteLinkAccount(msg.data, `${msg.transactionId}`);
  } else if (msg.uri === "post:/api/v1/linkAccounts/login") {
    return linkAccountService.loginLinkAccount(msg.data, `${msg.transactionId}`);
  } else if (msg.uri === "post:/api/v1/linkAccounts/confirm") {
    return linkAccountService.confirmLinkAccount(msg.data, `${msg.transactionId}`);
  } else if (msg.uri === "post:/api/v1/linkAccounts/unlink") {
    return linkAccountService.unlink(msg.data, `${msg.transactionId}`);
  } else if (msg.uri === "post:/api/v1/linkAccounts/init") {
    return linkAccountService.initLinkAccount(msg.data);
  } else if (msg.uri === "/api/v1/linkAccounts/notifyOtpPartner") {
    return linkAccountService.notifyOtpPartner(msg.data, `${msg.transactionId}`);
  } else if (msg.uri === "/api/v1/linkAccounts/notifyOtpFromPartner") {
    return linkAccountService.notifyOtpFromPartner(msg.data, `${msg.transactionId}`);
  } else if (msg.uri === "/api/v1/linkAccounts/changeUsername") {
    return linkAccountService.changeUsername(msg.data);
  } else if (msg.uri === "post:/api/v1/linkAccounts/approve") {
    return linkAccountService.createLinkAccountApprove(msg.data, `${msg.transactionId}`);
  } else if (msg.uri === "/api/v1/linkAccounts/getUserIdByPartnerName") {
    return linkAccountService.getUserIdByPartnerName(msg.data, `${msg.transactionId}`);
  } else if (msg.uri === "get:/api/v1/scopes/search") {
    const scopeResponse: Models.AAA.IScopeSearchRes = {
      scopes: scopeService.getScopesByScopeGroups(msg.data.scopeGroupIds).map((scope: Scope) => scope.getScopeResponse()),
    };
    return Promise.resolve(scopeResponse);
  } else if (msg.uri === "/api/v1/registerMobileOtp") {
    return registerMobileOtp(msg.data);
  } else if (msg.uri === "/api/v1/biometricRegister") {
    return registerBiometric(msg.data, msg);
  } else if (msg.uri === "/api/v1/biometricRegisterKis") {
    return registerBiometricKis(msg.data, msg);
  } else if (msg.uri === "/api/v1/verifyBiometricOtp") {
    return verifyBiometricOTP(msg.data);
  } else if (msg.uri === "/api/v1/unregisterBiometric") {
    return cancelBiometricRegister(msg.data);
  } else if (msg.uri === "/api/v1/unregisterMobileOtp") {
    return unregisterMobileOtp(msg.data);
  } else if (msg.uri === "/api/v1/queryBiometricStatus") {
    return queryBiometricStatus(msg.data);
  } else if (msg.uri === "/api/v1/notifyMobileOtp") {
    return notifyMobileOtp(msg.data, `${msg.transactionId}`);
  } else if (msg.uri === "/api/v1/notifyMobileOtpNhsv") {
    return notifyMobileOtpNhsv(msg.data, `${msg.transactionId}`);
  } else if (msg.uri === "/api/v1/notifyMobileOtpKisTtl") {
    return notifyMobileOtpKisTtl(msg.data, `${msg.transactionId}`);
  } else if (msg.uri === "/api/v1/notifyBiometricMobileOtp") {
    return notifyMobileBiometricOtp(msg.data, `${msg.transactionId}`);
  } else if (msg.uri === "/api/v1/importDbData") {
    return importDbJsonService.jsonDbImport(msg.data, msg);
  } else if (msg.uri === "/api/v1/updateProfile") {
    return updateProfile(msg.data);
  } else if (msg.uri === "/api/v1/verifyAndSaveOTP") {
    return generateSaveOtpToken(msg.data, msg);
  } else if (msg.uri === "/api/v1/verifyPwdBiometric") {
    return verifyPwdBiometric(msg.data, msg);
  } else if (msg.uri === "/api/v1/linkAccounts/leaderboard/settings") {
    return linkAccountService.putLeaderboardSetting(msg.data, `${msg.transactionId}`)
  } else if (msg.uri === "/api/v1/client/updateAppVersion") {
    return updateAppVersion(msg.data)
  }
  return handleMessage(msg);
}

export {
  handleAAAMessage,
};
