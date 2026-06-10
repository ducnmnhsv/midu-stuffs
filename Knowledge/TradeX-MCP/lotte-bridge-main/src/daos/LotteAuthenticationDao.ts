import { Inject, Service } from 'typedi';
import { Errors, Kafka, Logger, Utils } from 'tradex-common';
import config from '../config';
import {
  ILotteVerifyUserAccounts,
  ILotteVerifyUserBankInfo,
  ILotteVerifyUserDataList,
  ILotteVerifyUserResponse,
} from '../models/response/lotte/ILotteVerifyUserResponse';
import LotteCommonDao from './LotteCommonDao';
import { IContext } from '../models/IContext';
import Redis, { Category } from '../services/Redis';
import { ILotteVerifyOtpRequest } from '../models/request/lotte/ILotteVerifyOtpRequest';
import { ILotteVerifyOtpResponse } from '../models/response/lotte/ILotteVerifyOtpResponse';
import {
  ILoginResponse,
  IUserAccount,
  IUserBankAccount,
  IUserData,
  IUserInfo,
  IUserSubAccount,
  UserType,
} from '../models/response/ILoginResponse';
import { ILotteVerifyUserRequest } from '../models/request/lotte/ILotteVerifyUserRequest';
import { ILotteChangePinResponse } from '../models/response/lotte/ILotteChangePinResponse';
import { ILotteChangePinRequest } from '../models/request/lotte/ILotteChangePinRequest';
import { ILotteChangePasswordResponse } from '../models/response/lotte/ILotteChangePasswordResponse';
import { ILotteChangePasswordRequest } from '../models/request/lotte/ILotteChangePasswordRequest';
import { URLSearchParams } from 'url';
import { IVerifyUserData } from '../models/IVerifyUserData';
import { Constants } from '../constants/Constants';
import { getElementAtIndex, parseMessages } from '../utils/lotte';
import { IResetPasswordVerifyOtpRequest } from '../models/request/IResetPasswordVerifyOtpRequest';
import { IResetPasswordRequest } from '../models/request/IResetPasswordRequest';
import { IOtpData } from '../models/IOtpData';
import { IParam } from '../models/request/lotte/ILotteRequest';
import { ILotteResetPasswordRequest } from '../models/request/lotte/ILotteResetPasswordRequest';
import { IAccountInfoResponse } from '../models/response/IAccountInfoResponse';
import { IResetPasswordInitRequest } from '../models/request/IResetPasswordInitRequest';
import { ILotteResetPasswordResponse } from '../models/response/lotte/ILotteResetPasswordResponse';
import { IResetPasswordVerifyOtpResponse } from '../models/response/IResetPasswordVerifyOtpResponse';
import { IResetPasswordInitResponse } from '../models/response/IResetPasswordInitResponse';
import { v4 as uuidv4 } from 'uuid';
import { IChangePinResponse } from '../models/response/IChangePinResponse';
import { IChangePasswordResponse } from '../models/response/IChangePasswordResponse';
import * as moment from 'moment';
import { LOTTE_LANG_CODE } from '../constants/enum';
import { AccountBankInfo } from '../models/db/AccountBankInfo';
import { IOtpInfoResponse } from '../models/response/IOtpInfoResponse';
import { IOtpInfoRequest } from '../models/request/IOtpInfoRequest';
import { CommonService } from '../services/CommonService';
import * as _ from 'lodash';
import IPhoneNumberReq from '../models/request/IPhoneNumberReq';
import { InvalidParameterError } from 'tradex-common/build/src/modules/errors';
import { ILotteAccountInfoData, ILotteAccountInfoResponse } from '../models/response/lotte/ILotteAccountInfoResponse';
import { LotteAccountDao } from './LotteAccountDao';
import { ILotteAccountInfoRequest } from '../models/request/lotte/ILotteAccountInfoRequest';
import { HeaderTokenUserData } from '../models/db/HeaderTokenUserData';
import { InjectRepository } from 'typeorm-typedi-extensions';
import { HeaderTokenUserDataRepository } from '../repositories/HeaderTokenUserDataRepository';
import { AccountBankInfoRepository } from '../repositories/AccountBankInfoRepository';
import { checkStringTrim } from '../utils/defaultUtils';

const { GeneralError } = Errors;

@Service()
export class LotteAuthenticationDao {
  @Inject()
  private lotteCommonDao: LotteCommonDao;
  @Inject()
  private redis: Redis;
  @Inject()
  private commonService: CommonService;
  private ACCOUNT_REGEX: RegExp = /^.{3}[A-Za-z].{6}/;
  @Inject()
  private lotteAccountDao: LotteAccountDao;
  @InjectRepository()
  private headerTokenUserDataRepository: HeaderTokenUserDataRepository;
  @InjectRepository()
  private accountBankInfoRepository: AccountBankInfoRepository;

  async verifyUser(
    username: string,
    password: string,
    sourceIp: string,
    language: string,
    isGetOtp: boolean,
    ctx: IContext
  ): Promise<ILoginResponse> {
    if (config.turnOnTrimPassword) {
      password = password.substr(0, config.passwordLength);
    } else {
      if (password.length > config.passwordLength) {
        throw new InvalidParameterError().add('PASSWORD_IS_TOO_LONG', 'password', [
          `Password must not be longer than ${config.passwordLength} characters`,
        ]);
      }
    }

    const body: ILotteVerifyUserRequest = {
      username: username.toLowerCase(),
      password,
      useraddr: sourceIp,
      lang_code:
        language == null || LOTTE_LANG_CODE[language] == null ? config.defaultLanguage : LOTTE_LANG_CODE[language],
    };
    const lotteResponseData: ILotteVerifyUserDataList = await this.callVerifyUser(body, ctx);
    if (lotteResponseData.accounts != null) {
      this.storeAccountBankInfo(lotteResponseData.accounts, lotteResponseData.login_id.toUpperCase(), ctx);
    }
    let response: ILoginResponse;
    if (isGetOtp) {
      const otpKey: string = uuidv4();
      const redisData = {
        authen_result: lotteResponseData,
        otp_key: otpKey,
      };
      this.redis.set(Category.AUTH_OTP_DATA, otpKey, redisData, config.otpLifeTime.sms.DEFAULT_EXPIRE_TIME);
      const userData: IUserData = this.toILoginResponse(lotteResponseData).userData;
      response = {
        otpIndex: lotteResponseData.otp_index,
        registerMobileOtp: false,
        userData: {
          ...userData,
          mfaData: otpKey,
        },
        sessionId: lotteResponseData.login_id,
      };
    } else {
      response = this.toILoginResponse(lotteResponseData);
      const verifyUserData: IVerifyUserData = {
        sec_pwd: lotteResponseData.sec_pwd,
        acnt_scrt: getElementAtIndex<ILotteVerifyUserAccounts>(lotteResponseData.accounts).acnt_scrt,
      };
      this.redis.set(Category.VERIFY_USER_DATA, lotteResponseData.login_id, verifyUserData);
    }
    this.storeHeaderTokenUserData(response.userData, ctx);
    return response;
  }

  private storeAccountBankInfo(accounts: ILotteVerifyUserAccounts[], loginId: string, ctx: IContext) {
    const accountBankInfos: AccountBankInfo[] = accounts.reduce((result, account) => {
      const bankInfos: AccountBankInfo[] = account.bankInfo
        .map((bank) => {
          const accountBankInfo = new AccountBankInfo();
          accountBankInfo.username = loginId.toUpperCase();
          accountBankInfo.subNumber = account.acnt_no.slice(-2);
          accountBankInfo.bankCode = bank.bank_code;
          accountBankInfo.bankName = bank.bank_name;
          return accountBankInfo;
        })
        .filter((bankInfo) => !Utils.isEmpty(checkStringTrim(bankInfo.bankCode)));
      return [...result, ...bankInfos];
    }, []);
    return this.accountBankInfoRepository
      .createQueryBuilder()
      .insert()
      .into(AccountBankInfo)
      .orUpdate({ conflict_target: ['username', 'subNumber', 'bankCode'], overwrite: ['bankName', 'bankAccount'] })
      .values(accountBankInfos)
      .execute();
  }

  private storeHeaderTokenUserData(userData: IUserData, ctx: IContext) {
    const headerTokenUserData: HeaderTokenUserData = new HeaderTokenUserData();
    headerTokenUserData.accountNumber = userData.username.toUpperCase();
    headerTokenUserData.userData = JSON.stringify(userData);
    return this.headerTokenUserDataRepository
      .createQueryBuilder()
      .insert()
      .into(HeaderTokenUserData)
      .orUpdate({
        conflict_target: ['username'],
        overwrite: ['userData'],
      })
      .values(headerTokenUserData)
      .execute();
  }

  async getAuthenticateOtpInfo(request: IOtpInfoRequest, ctx: IContext): Promise<IOtpInfoResponse> {
    const accountNumber: string = request.headers.token.userData.accountNumbers[0];
    const prefixLog = `ctxId: ${ctx.id}, accountNumber: ${accountNumber}`;
    if (_.isEmpty(accountNumber)) {
      throw new GeneralError(`${prefixLog} -- ${Constants.ACCOUNT_NUMBER_IS_EMPTY}`);
    }
    const otpKey: string = request.headers.token.userData.mfaData;
    if (_.isEmpty(otpKey)) {
      throw new GeneralError(`${prefixLog} -- ${Constants.TOKEN_REQUEST_MFA_DATA_FIELD_IS_EMPTY}`);
    }

    const authRedisKey = this.redis.getRedisKey(Category.AUTH_OTP_DATA, otpKey);
    const isExistsAuthRedisKey = await this.redis.exists(authRedisKey);
    Logger.info(`${prefixLog} -- isExistsAuthRedisKey = ${isExistsAuthRedisKey}`);
    if (!isExistsAuthRedisKey) {
      throw new GeneralError(`${prefixLog}, redis key = ${authRedisKey} is not exists`);
    }
    const redisData = await this.redis.get(Category.AUTH_OTP_DATA, otpKey);
    if (redisData == null) {
      throw new GeneralError(`${prefixLog} -- ${Constants.OTP_NOT_EXIST}`);
    }
    const authenResult: ILotteVerifyUserDataList = redisData['authen_result'] as ILotteVerifyUserDataList;
    Logger.info(`${prefixLog} -- authenResult: ${JSON.stringify(authenResult)}`);
    const mobileOtp: string = this.commonService.decodeOtp(authenResult.otp_pass, ctx.id);
    if (_.isEmpty(mobileOtp) || _.isEmpty(mobileOtp.trim())) {
      throw new GeneralError(`${prefixLog} -- mobileOtp is empty`);
    }
    let phoneNumber: string = null;
    if (config.lotte.isGetPhoneNumberFromTuxedo) {
      const phoneNumberRequest: IPhoneNumberReq = {
        accountNumber: request.headers.token.userData.accountNumbers[0],
        headers: request.headers,
        macAddress: request.macAddress,
        platform: request.platform,
        osVersion: request.osVersion,
        appVersion: request.appVersion,
        sourceIp: request.sourceIp,
      };
      phoneNumber = await this.commonService.getPhoneNumberFromTuxedo(phoneNumberRequest, ctx.txId);
    } else {
      let userInfo: IAccountInfoResponse = null;
      const userAccInfoRedisKey = this.redis.getRedisKey(Category.USER_ACC_INFO, accountNumber);
      const isExistsUserAccInfoRedisKey = await this.redis.exists(userAccInfoRedisKey);
      Logger.info(`${prefixLog} -- isExistsUserAccInfoRedisKey = ${isExistsUserAccInfoRedisKey}`);
      if (isExistsUserAccInfoRedisKey) {
        userInfo = await this.redis.get(Category.USER_ACC_INFO, accountNumber);
      }
      if (_.isEmpty(userInfo)) {
        userInfo = await this.commonService.getAccountInfo(accountNumber, ctx);
        if (_.isEmpty(userInfo)) {
          throw new GeneralError(`${prefixLog} -- account info is empty`);
        }
      }
      Logger.info(`${prefixLog} -- userInfo: ${JSON.stringify(userInfo)}`);
      phoneNumber = userInfo['phoneNumber'];
    }
    if (_.isEmpty(phoneNumber) || _.isEmpty(phoneNumber.trim())) {
      throw new GeneralError(`${prefixLog} -- phone number is empty`);
    }

    const response: IOtpInfoResponse = {
      phoneNumber: this.commonService.convertPhoneNumber(phoneNumber),
      otpValue: mobileOtp,
    };
    Logger.info(`${prefixLog} -- response: ${JSON.stringify(response)}`);
    return response;
  }

  private async callVerifyUser(body: ILotteVerifyUserRequest, ctx: IContext): Promise<ILotteVerifyUserDataList> {
    const formData = new URLSearchParams();
    const keys: string[] = Object.keys(body);
    for (const key of keys) {
      formData.append(key, body[key]);
    }
    const lotteResponse: ILotteVerifyUserResponse = await this.lotteCommonDao.post<ILotteVerifyUserResponse>(
      config.lotte.apis.verifyUser,
      {
        'Content-Type': 'application/x-www-form-urlencoded',
      },
      formData.toString(),
      ctx
    );
    const { codes, messages } = parseMessages(lotteResponse.error_desc, lotteResponse.error_code);
    Logger.info(ctx.id, 'codes', codes, 'messages', messages);
    const lotteResponseData: ILotteVerifyUserDataList = getElementAtIndex<ILotteVerifyUserDataList>(
      lotteResponse.data_list
    );
    if (codes == null || codes === '0001') {
      if (lotteResponseData.otp_stat === 'P') {
        throw new GeneralError(Constants.LOGIN_WRONG_PASSWORD_5_TIMES);
      }
      return lotteResponseData;
    } else if (codes === 'XX') {
      throw new GeneralError(Constants.LOGIN_WRONG_PASSWORD, null, null, [messages]);
    } else if (codes === '2016') {
      throw new GeneralError(lotteResponse.error_desc);
    } else {
      throw new GeneralError(`${Constants.ACCOUNT_LOGIN}${codes}`);
    }
  }

  async verifyOTP(otpValue: string, otpKey: string, ctx: IContext): Promise<ILoginResponse> {
    const redisData = await this.getAuthenOtpData(otpKey, ctx);
    if (redisData == null) {
      throw new GeneralError(Constants.OTP_NOT_EXIST);
    }
    const authenResult: ILotteVerifyUserDataList = redisData['authen_result'] as ILotteVerifyUserDataList;
    const body: ILotteVerifyOtpRequest = {
      acnt_no: authenResult.login_id.toUpperCase(),
      otp_val: otpValue,
      otp_enc: authenResult.otp_pass,
      otp_ind: authenResult.otp_index,
    };
    const lotteResponse: ILotteVerifyOtpResponse = await this.lotteCommonDao.post<ILotteVerifyOtpResponse>(
      config.lotte.apis.verifyOtp,
      null,
      body,
      ctx
    );
    const { codes, messages } = parseMessages(lotteResponse.error_desc, lotteResponse.error_code);
    Logger.info(ctx.id, 'codes', codes, 'messages', messages);
    const lotteResponseDataList: IParam = getElementAtIndex<IParam>(lotteResponse.data_list);
    if (codes === null || codes === '0011') {
      const verifyUserData: IVerifyUserData = {
        sec_pwd: authenResult.sec_pwd,
        acnt_scrt: getElementAtIndex<ILotteVerifyUserAccounts>(authenResult.accounts).acnt_scrt,
      };
      this.redis.set(Category.VERIFY_USER_DATA, authenResult.login_id, verifyUserData);
      return this.toILoginResponse(authenResult);
    } else if (codes != null && codes === '2009') {
      throw new GeneralError(Constants.WRONG_OTP, null, null, [lotteResponseDataList.scrt_err_msg]);
    } else {
      throw new GeneralError();
    }
  }

  private async getAuthenOtpData(otpKey: string, ctx: IContext) {
    try {
      return await this.redis.get(Category.AUTH_OTP_DATA, otpKey);
    } catch (error) {
      Logger.error(ctx.id, 'error get authen otp data', error);
      return null;
    }
  }

  private toILoginResponse(verifyLotteResDataList: ILotteVerifyUserDataList): ILoginResponse {
    const map: Map<string, IUserAccount> = new Map<string, IUserAccount>();
    const bankInfo = {};
    if (verifyLotteResDataList.accounts != null && verifyLotteResDataList.accounts.length > 0) {
      verifyLotteResDataList.accounts.forEach((account: ILotteVerifyUserAccounts) => {
        const bankAccount: IUserBankAccount[] = account.bankInfo.map((bank: ILotteVerifyUserBankInfo) => ({
          bankCode: bank.bank_code,
          bankName: bank.bank_name,
        }));
        const match: RegExpMatchArray | null = account.acnt_no.match(this.ACCOUNT_REGEX);
        const accountNumber: string = match ? match[0] : account.acnt_no;
        const subNumber: string = account.acnt_no.slice(accountNumber.length, account.acnt_no.length);
        const accountSub: IUserSubAccount = {
          subNumber,
          bankAccounts: bankAccount,
          type: 'EQUITY',
        };
        bankInfo[subNumber] = [
          ...new Set(
            bankAccount
              .filter((bank: IUserBankAccount) => !Utils.isEmpty(checkStringTrim(bank.bankCode)))
              .map((bank: IUserBankAccount) => bank.bankCode)
          ),
        ];
        if (map.has(accountNumber)) {
          map.get(accountNumber).accountSubs.push(accountSub);
        } else {
          map.set(accountNumber, {
            accountNumber,
            accountName: account.acnt_nm,
            accountSubs: [accountSub],
          });
        }
      });
    }
    const userAccounts: IUserAccount[] = Array.from(map.values()).sort((acc1, acc2) =>
      acc1.accountNumber && acc2.accountNumber && acc1.accountNumber.toLowerCase() < acc2.accountNumber.toLowerCase()
        ? 1
        : -1
    );

    // NHSV
    // 00 : Admin :  Admin
    // 01 : 관리자(영업관리 : manager (sales management)
    // 02 : 영업사원, 투자상담사 : Salesperson, investment advisor
    // 03 : 시장부 : Market department
    // 04 : 본사후선부서 : Head office post office
    // 07 : 객장단말 사용 ID : ID for using the guest terminal
    // 09 : 비밀번호 변경 전 일반고객 :  General customers before password change
    // 0A : HTS 초급고객 : HTS beginner customers
    // 0B : HTS 고급고객 : HTS high-end customers
    // 0C : 제한 등급자 : limited grader
    // 0D : 비밀번호 변경 후 일반고객 : General customers after password change

    let userLevel: string = null;
    let requireChangePassword: boolean = false;
    if (
      verifyLotteResDataList.hts_level === '0C' ||
      verifyLotteResDataList.hts_level === '0B' ||
      verifyLotteResDataList.hts_level === '0D' ||
      verifyLotteResDataList.hts_level === '0Z'
    ) {
      userLevel = UserType.USER;
    } else if (verifyLotteResDataList.hts_level === '0') {
      userLevel = UserType.ADMIN;
    } else if (verifyLotteResDataList.hts_level === '2' || verifyLotteResDataList.hts_level === '13') {
      userLevel = UserType.BROKER;
    } else if (verifyLotteResDataList.hts_level === '0A') {
      userLevel = UserType.USER_CHANGE_PASSWORD_REQUIRED;
      requireChangePassword = true;
    }
    const userData: IUserData = {
      username: verifyLotteResDataList.login_id,
      identifierNumber: verifyLotteResDataList.id_no,
      deptCode: verifyLotteResDataList.dept_code1,
      branchCode: verifyLotteResDataList.dept_code2,
      mngDeptCode: verifyLotteResDataList.dept_code0,
      agencyNumber: verifyLotteResDataList.agc_no,
      userType: verifyLotteResDataList.hts_level,
      name: verifyLotteResDataList.user_name,
      accountNumbers: userAccounts.map((account: IUserAccount) => account.accountNumber),
      bankInfo,
      userLevel,
    };
    const userInfo: IUserInfo = {
      username: verifyLotteResDataList.login_id.toUpperCase(),
      identifierNumber: verifyLotteResDataList.id_no,
      accounts: userAccounts,
      userLevel,
      requireChangePassword,
      branchCode: verifyLotteResDataList.dept_code1,
    };
    const response: ILoginResponse = {
      userData,
      userInfo,
      sessionId: verifyLotteResDataList.login_id,
    };
    return response;
  }

  async changePassword(
    loginId: string,
    htsUserId: string,
    htsId: string,
    newPassword: string,
    oldPassword: string,
    language: string,
    ctx: IContext
  ): Promise<IChangePasswordResponse> {
    const verifyUserData: IVerifyUserData = await this.getVerifyUserData(loginId, ctx);
    if (verifyUserData == null) {
      throw new GeneralError(Constants.UNAUTHORIZED);
    }
    if (config.turnOnTrimPassword) {
      newPassword = newPassword.substr(0, config.passwordLength);
      oldPassword = oldPassword.substr(0, config.passwordLength);
    } else {
      if (newPassword.length > config.passwordLength) {
        throw new InvalidParameterError().add('PASSWORD_IS_TOO_LONG', 'newPassword', [
          `Password must not be longer than ${config.passwordLength} characters`,
        ]);
      }
    }

    const secPwd: string = verifyUserData.sec_pwd;
    const body: ILotteChangePasswordRequest = {
      hts_user_id: htsUserId.toLowerCase(),
      hts_id: htsId.toLowerCase(),
      sec_pass: secPwd,
      old_pass: oldPassword,
      new_pass: newPassword,
    };
    const lotteResponse: ILotteChangePasswordResponse = await this.lotteCommonDao.post<ILotteChangePasswordResponse>(
      config.lotte.apis.changePassword,
      null,
      body,
      ctx
    );
    const { codes, messages } = parseMessages(lotteResponse.error_desc, lotteResponse.error_code);
    Logger.info(ctx.id, 'codes', codes, 'messages', messages);
    if (codes === null || codes === '0008') {
      try {
        const bodyVerifyUser: ILotteVerifyUserRequest = {
          username: htsId.toLowerCase(),
          password: newPassword,
          useraddr: ctx.orgMsg.data.sourceIp,
          lang_code:
            language == null || LOTTE_LANG_CODE[language] == null ? config.defaultLanguage : LOTTE_LANG_CODE[language],
        };
        const verifyUserRes: ILotteVerifyUserDataList = await this.callVerifyUser(bodyVerifyUser, ctx);
        const verifyUserData: IVerifyUserData = {
          sec_pwd: verifyUserRes.sec_pwd,
          acnt_scrt: getElementAtIndex<ILotteVerifyUserAccounts>(verifyUserRes.accounts).acnt_scrt,
        };
        this.redis.set(Category.VERIFY_USER_DATA, verifyUserRes.login_id, verifyUserData);
      } catch (error) {
        Logger.error(ctx.id, 'error verify user', error);
      }
      return {
        message: messages,
      };
    } else {
      throw new GeneralError(`${Constants.ACCOUNT_CHANGE_PASSWORD}${codes}}`);
    }
  }

  async changePin(
    loginId: string,
    htsUserId: string,
    htsId: string,
    newPassword: string,
    oldPassword: string,
    deptCode: string,
    ctx: IContext
  ): Promise<IChangePinResponse> {
    const verifyUserData: IVerifyUserData = await this.getVerifyUserData(loginId, ctx);
    if (verifyUserData == null) {
      throw new GeneralError(Constants.UNAUTHORIZED);
    }
    const deptNo1: string = deptCode;
    const acntScrt: string = verifyUserData.acnt_scrt;
    const body: ILotteChangePinRequest = {
      hts_user_id: htsUserId.toLowerCase(),
      dept_no1: deptNo1,
      hts_id: htsId.toUpperCase(),
      old_enc_pass: acntScrt,
      old_pass: oldPassword,
      new_pass: newPassword,
    };
    const lotteResponse: ILotteChangePinResponse = await this.lotteCommonDao.post<ILotteChangePinResponse>(
      config.lotte.apis.changePin,
      null,
      body,
      ctx
    );
    const { codes, messages } = parseMessages(lotteResponse.error_desc, lotteResponse.error_code);
    Logger.info(ctx.id, 'codes', codes, 'messages', messages);
    if (codes === null || codes === '0008') {
      return {
        message: messages,
      };
    } else {
      throw new GeneralError(`${Constants.ACCOUNT_CHANGE_PASSWORD}${codes}`);
    }
  }

  private async getVerifyUserData(loginId: string, ctx: IContext) {
    try {
      return await this.redis.get<IVerifyUserData>(Category.VERIFY_USER_DATA, loginId);
    } catch (error) {
      Logger.error(ctx.id, 'error get verify user data', error);
      return null;
    }
  }

  async resetPasswordInit(request: IResetPasswordInitRequest, ctx: IContext): Promise<IResetPasswordInitResponse> {
    let otpValidate = await this.redis.getOtpValidate(request.phoneNumber, ctx);
    if (otpValidate != null) {
      if (otpValidate.count > config.otpMaxGenTime) {
        throw new GeneralError(Constants.OTP_LIMIT_GENERATE);
      }
      if (
        moment(otpValidate.latestRequest)
          .add(config.otpGenTime, 'seconds')
          .isAfter(moment())
      ) {
        throw new GeneralError(Constants.OTP_GENERATE_TO_FAST);
      }
      otpValidate.count = otpValidate.count + 1;
      otpValidate.latestRequest = new Date();
    } else {
      otpValidate = {
        count: 0,
        username: request.accountNumber,
        latestRequest: new Date(),
      };
    }
    let userAccountInfo: IAccountInfoResponse = null;
    const lotteAccountInfoRequest: ILotteAccountInfoRequest = {
      acnt_no: request.accountNumber,
    };
    const lotteRes: ILotteAccountInfoResponse = await this.lotteAccountDao.getAccountInfo(lotteAccountInfoRequest, ctx);
    const { codes } = parseMessages(lotteRes.error_desc, lotteRes.error_code);
    const lotteResDataList: ILotteAccountInfoData = getElementAtIndex<ILotteAccountInfoData>(lotteRes.data_list);
    if (codes === null || codes === '0011') {
      userAccountInfo = {
        username: request.accountNumber.toUpperCase(),
        email: lotteResDataList.email,
        address: lotteResDataList.address,
        phoneNumber: lotteResDataList.phone,
        identifierNumber: lotteResDataList.identity_card,
        customerName: lotteResDataList.customer_name,
        agencyName: lotteResDataList.manager,
      };
    } else {
      throw new GeneralError(lotteRes.error_desc);
    }

    if (userAccountInfo == null) {
      throw new GeneralError(Constants.WRONG_ACCOUNT_INFORMATION);
    }
    if (userAccountInfo.phoneNumber !== request.phoneNumber) {
      throw new GeneralError(Constants.WRONG_ACCOUNT_INFORMATION);
    }
    if (userAccountInfo.identifierNumber !== request.identifierNumber) {
      throw new GeneralError(Constants.WRONG_ACCOUNT_INFORMATION);
    }
    const otpKey: string = uuidv4();
    const otpValue: number = Math.floor(Math.random() * (config.maxOtpNumber + 1));
    const otp = otpValue.toString().padStart(config.otpLength, '0');
    const redisData: IOtpData = {
      request,
      otp_key: otpKey,
      otp,
      step: Constants.STEP_WAIT_VERIFY_OTP,
    };
    await this.redis.setOtpValidate(request.phoneNumber, otpValidate);
    await this.redis.set(Category.RESET_PASSWORD_DATA, otpKey, redisData, config.otpLifeTime.sms.RESET_PASSWORD);
    let phoneNumber: string = request.phoneNumber;
    if (phoneNumber.startsWith('0')) {
      phoneNumber = `84${phoneNumber.substring(1)}`;
    }
    if (phoneNumber.startsWith('+')) {
      phoneNumber = phoneNumber.substring(1);
    }
    Kafka.getInstance().sendMessage(ctx.txId, 'notification', '', {
      method: config.notiMethod.SMS,
      template: {
        nhsv_reset_password_otp: {
          otp,
        },
      },
      configuration: JSON.stringify({
        phoneNumber,
      }),
      domain: 'nhsv',
      locale: request.headers['accept-language'] == null ? 'vi' : request.headers['accept-language'],
    });
    const response: IResetPasswordInitResponse = {
      otpKey,
    };
    return response;
  }

  async resetPasswordVerifyOtp(
    request: IResetPasswordVerifyOtpRequest,
    ctx: IContext
  ): Promise<IResetPasswordVerifyOtpResponse> {
    const redisData: IOtpData = await this.redis.getResetPasswordData(request.otpKey, ctx);
    if (redisData == null || redisData.step !== Constants.STEP_WAIT_VERIFY_OTP) {
      throw new GeneralError(Constants.OTP_NOT_EXIST);
    }
    if (redisData.otp !== request.otpValue) {
      throw new GeneralError(Constants.WRONG_OTP);
    }
    redisData.step = Constants.STEP_VERIFIED_OTP;
    await this.redis.set(
      Category.RESET_PASSWORD_DATA,
      request.otpKey,
      redisData,
      config.otpLifeTime.sms.RESET_PASSWORD
    );
    const response: IResetPasswordVerifyOtpResponse = {
      otpKey: request.otpKey,
    };
    return response;
  }

  async resetPassword(request: IResetPasswordRequest, ctx: IContext): Promise<IParam> {
    const redisData: IOtpData = await this.redis.getResetPasswordData(request.otpKey, ctx);
    if (redisData == null || redisData.step !== Constants.STEP_VERIFIED_OTP) {
      throw new GeneralError(Constants.OTP_NOT_EXIST);
    }
    const lotteRequest: ILotteResetPasswordRequest = {
      hts_user_id: redisData.request['accountNumber'].toLowerCase(),
      user_id: redisData.request['accountNumber'].toLowerCase(),
      id_no: redisData.request['identifierNumber'],
      phone_no: redisData.request['phoneNumber'],
      pswd_new: request.password,
      cli_ip_addr: ctx.orgMsg.data.sourceIp,
      dept_no1: config.defaultDeptCode,
    };
    const lotteResponse: ILotteResetPasswordResponse = await this.lotteCommonDao.post<ILotteResetPasswordResponse>(
      config.lotte.apis.resetPassword,
      null,
      lotteRequest,
      ctx
    );
    const { codes, messages } = parseMessages(lotteResponse.error_desc, lotteResponse.error_code);
    Logger.info(ctx.id, 'codes', codes, 'messages', messages);
    if (codes === null || codes === '0008') {
      return {
        message: messages,
      };
    } else {
      throw new GeneralError(`${Constants.ACCOUNT_CHANGE_HTS_PASSWORD}${codes}}`);
    }
  }
}
