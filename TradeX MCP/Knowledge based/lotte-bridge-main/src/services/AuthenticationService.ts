import { Inject, Service } from 'typedi';
import { ILoginRequest } from '../models/request/ILoginRequest';
import { IOtpInfoRequest } from '../models/request/IOtpInfoRequest';
import { Errors, Logger, Utils } from 'tradex-common';
import config from '../config';
import { rsaPrivateKey } from '../utils/rsa';
import { LotteAuthenticationDao } from '../daos/LotteAuthenticationDao';
import { IContext } from '../models/IContext';
import { ILoginResponse } from '../models/response/ILoginResponse';
import { IVerifyOtpRequest } from '../models/request/IVerifyOtpRequest';
import { IChangePasswordRequest } from '../models/request/IChangePasswordRequest';
import { IChangePasswordResponse } from '../models/response/IChangePasswordResponse';
import { IChangePinRequest } from '../models/request/IChangePinRequest';
import { IChangePinResponse } from '../models/response/IChangePinResponse';
import { IResetPasswordRequest } from '../models/request/IResetPasswordRequest';
import { IResetPasswordInitRequest } from '../models/request/IResetPasswordInitRequest';
import { IResetPasswordVerifyOtpRequest } from '../models/request/IResetPasswordVerifyOtpRequest';
import { IParam } from '../models/request/lotte/ILotteRequest';
import { IResetPasswordVerifyOtpResponse } from '../models/response/IResetPasswordVerifyOtpResponse';
import { IResetPasswordInitResponse } from '../models/response/IResetPasswordInitResponse';
import { Constants } from '../constants/Constants';
import { validateRequestAccountNoCreator } from '../utils/lotte';
import { IOtpInfoResponse } from '../models/response/IOtpInfoResponse';

const { InvalidParameterError, GeneralError } = Errors;
const { validate } = Utils;

@Service()
export class AuthenticationService {
  @Inject()
  private lotteAuthenticationDao: LotteAuthenticationDao;

  async authenticate(request: ILoginRequest, ctx: IContext): Promise<ILoginResponse> {
    let password = request.password;
    if (config.enableEncryptPassword) {
      password = Utils.rsaDecrypt(request.password, rsaPrivateKey);
    }
    const body: ILoginRequest = {
      username: request.username,
      password,
    };
    const language: string = request.headers ? request.headers['accept-language'] : null;
    const response: ILoginResponse = await this.lotteAuthenticationDao.verifyUser(
      body.username,
      body.password,
      ctx.orgMsg.data.sourceIp,
      language,
      false,
      ctx
    );
    return response;
  }

  async authenticateOtp(request: ILoginRequest, ctx: IContext): Promise<ILoginResponse> {
    let password = request.password;
    if (config.enableEncryptPassword) {
      password = Utils.rsaDecrypt(request.password, rsaPrivateKey);
    }
    const body: ILoginRequest = {
      username: request.username,
      password,
    };
    const language: string = request.headers ? request.headers['accept-language'] : null;
    const response: ILoginResponse = await this.lotteAuthenticationDao.verifyUser(
      body.username,
      body.password,
      ctx.orgMsg.data.sourceIp,
      language,
      true,
      ctx
    );
    return response;
  }

  async getAuthenticateOtpInfo(request: IOtpInfoRequest, ctx: IContext): Promise<IOtpInfoResponse> {
    Logger.info(`ctxId: ${ctx.id}, request: ${JSON.stringify(request)}`);
    return this.lotteAuthenticationDao.getAuthenticateOtpInfo(request, ctx);
  }

  async verifyOTP(request: IVerifyOtpRequest, ctx: IContext): Promise<ILoginResponse> {
    const error = new InvalidParameterError();
    validate(request.otpValue, 'otpValue')
      .setRequire()
      .throwValid(error);
    error.throwErr();
    if (request.headers == null || request.headers.token == null || request.headers.token.userData == null) {
      throw new GeneralError(Constants.UNAUTHORIZED);
    }
    const otpKey: string = request.headers.token.userData.mfaData;
    if (otpKey == null || otpKey === '') {
      throw new GeneralError(Constants.UNAUTHORIZED);
    }
    const response: ILoginResponse = await this.lotteAuthenticationDao.verifyOTP(request.otpValue, otpKey, ctx);
    return response;
  }

  async changePassword(request: IChangePasswordRequest, ctx: IContext): Promise<IChangePasswordResponse> {
    const error = new InvalidParameterError();
    validate(request.username, 'username')
      .setRequire()
      .throwValid(error);
    validate(request.newPassword, 'newPassword')
      .setRequire()
      .throwValid(error);
    validate(request.oldPassword, 'oldPassword')
      .setRequire()
      .throwValid(error);
    error.throwErr();
    let newPassword = request.newPassword;
    let oldPassword = request.oldPassword;
    if (config.enableEncryptPassword) {
      newPassword = Utils.rsaDecrypt(request.newPassword, rsaPrivateKey);
    }
    if (config.enableEncryptPassword) {
      oldPassword = Utils.rsaDecrypt(request.oldPassword, rsaPrivateKey);
    }
    const htsUserId: string = request.username;
    const htsId: string = request.headers.token.userData.username;
    const loginId: string = request.headers.token.userData.username.toLowerCase();
    const language: string = request.headers['accept-language'];
    const response: IChangePasswordResponse = await this.lotteAuthenticationDao.changePassword(
      loginId,
      htsUserId,
      htsId,
      newPassword,
      oldPassword,
      language,
      ctx
    );
    return response;
  }

  async changePin(request: IChangePinRequest, ctx: IContext): Promise<IChangePinResponse> {
    const error = new InvalidParameterError();
    validate(request.accountNumber, 'accountNumber')
      .setRequire()
      .add(validateRequestAccountNoCreator(request))
      .throwValid(error);
    validate(request.newPassword, 'newPassword')
      .setRequire()
      .throwValid(error);
    validate(request.oldPassword, 'oldPassword')
      .setRequire()
      .throwValid(error);
    error.throwErr();
    let newPassword = request.newPassword;
    let oldPassword = request.oldPassword;
    if (config.enableEncryptPassword) {
      newPassword = Utils.rsaDecrypt(request.newPassword, rsaPrivateKey);
    }
    if (config.enableEncryptPassword) {
      oldPassword = Utils.rsaDecrypt(request.oldPassword, rsaPrivateKey);
    }
    const htsUserId: string = request.accountNumber;
    const htsId: string = request.headers.token.userData.username;
    const loginId: string = request.headers.token.userData.username.toLowerCase();
    const deptCode: string = request.headers.token.userData.deptCode;
    const response: IChangePinResponse = await this.lotteAuthenticationDao.changePin(
      loginId,
      htsUserId,
      htsId,
      newPassword,
      oldPassword,
      deptCode,
      ctx
    );
    return response;
  }

  async resetPasswordInit(request: IResetPasswordInitRequest, ctx: IContext): Promise<IResetPasswordInitResponse> {
    const error = new InvalidParameterError();
    validate(request.accountNumber, 'accountNumber')
      .setRequire()
      .throwValid(error);
    validate(request.phoneNumber, 'phoneNumber')
      .setRequire()
      .throwValid(error);
    validate(request.identifierNumber, 'identifierNumber')
      .setRequire()
      .throwValid(error);
    error.throwErr();
    const response: IResetPasswordInitResponse = await this.lotteAuthenticationDao.resetPasswordInit(request, ctx);
    return response;
  }

  async resetPasswordVerifyOtp(
    request: IResetPasswordVerifyOtpRequest,
    ctx: IContext
  ): Promise<IResetPasswordVerifyOtpResponse> {
    const error = new InvalidParameterError();
    validate(request.otpKey, 'otpKey')
      .setRequire()
      .throwValid(error);
    validate(request.otpValue, 'otpValue')
      .setRequire()
      .throwValid(error);
    error.throwErr();
    const response: IResetPasswordVerifyOtpResponse = await this.lotteAuthenticationDao.resetPasswordVerifyOtp(
      request,
      ctx
    );
    return response;
  }

  async resetPassword(request: IResetPasswordRequest, ctx: IContext): Promise<IParam> {
    const error = new InvalidParameterError();
    validate(request.otpKey, 'otpKey')
      .setRequire()
      .throwValid(error);
    validate(request.password, 'password')
      .setRequire()
      .throwValid(error);
    error.throwErr();
    if (config.enableEncryptPassword) {
      request.password = Utils.rsaDecrypt(request.password, rsaPrivateKey);
    }
    const response: IParam = await this.lotteAuthenticationDao.resetPassword(request, ctx);
    return response;
  }
}
