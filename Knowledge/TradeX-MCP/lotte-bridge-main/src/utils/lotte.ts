import { Errors, Models, Utils } from 'tradex-common';
import config from '../config';
import { IAPI } from '../models/IAPI';
import { AccountBankInfo } from '../models/db/AccountBankInfo';
import { AccountBankInfoRepository } from '../repositories/AccountBankInfoRepository';

const { GeneralError } = Errors;
const { createFailValidation, createSuccessValidation } = Utils;

export function getUrl(apiUri: IAPI): string {
  let url: string;
  url = config.lotte.baseUrl[apiUri.category];
  return `${url}/${apiUri.api}`;
}

export function parseMessages(errorDesc: string, errorCode: string): { codes: string; messages: string } {
  let codes: string = null;
  let messages: string = null;
  if (errorDesc.length > 0) {
    const startIndex = errorDesc.indexOf('[');
    const endIndex = errorDesc.indexOf(']');
    if (startIndex >= 0 && endIndex > 0) {
      codes = errorDesc.substring(startIndex + 2, endIndex);
      messages = errorDesc.substring(endIndex + 1);
    }
    if (codes === null) {
      codes = 'INTERNAL_SERVER_ERROR';
    }
  }
  // NHSV: 2016-Không có dữ liệu
  if (config.lotte.errorCodeBusiness.includes(errorCode) && codes !== '2016') {
    throw new GeneralError(errorDesc);
  }
  return { codes, messages };
}

export function getElementAtIndex<T>(arr: T[], defaultValue: T = {} as T, index: number = 0): T {
  if (arr == null) {
    return defaultValue;
  }
  if (arr.length < 1) {
    return defaultValue;
  } else if (arr.length <= index) {
    return arr[0];
  } else {
    return arr[index];
  }
}

export function setDefault<T>(value: T, defaultValue: T): T {
  if (typeof value === 'string' && Utils.isEmpty(value)) {
    return defaultValue;
  }
  return value ?? defaultValue;
}

function isAccountNoValid(accountNumber: string, tokenUserData: Models.IUserData): boolean {
  if (Utils.isEmpty(accountNumber) || tokenUserData == null) {
    return false;
  }
  if (tokenUserData.accountNumbers != null) {
    return tokenUserData.accountNumbers.includes(accountNumber);
  }
  return false;
}

function validationAccountNoCreator(userData: Models.IUserData) {
  return (accountNo: string, fieldName: string) => {
    if (isAccountNoValid(accountNo, userData)) {
      return createSuccessValidation(accountNo);
    } else {
      return createFailValidation('INVALID_ACCOUNT_NUMBER', [fieldName, accountNo], fieldName);
    }
  };
}

export function validateRequestAccountNoCreator<T extends Models.IDataRequest>(request: T) {
  if (request.headers.token.userData == null && request.headers.token.grantType !== 'client_credentials') {
    return (accountNo: string, fieldName: string) =>
      createFailValidation('INVALID_ACCOUNT_NUMBER', [fieldName, accountNo], fieldName);
  }
  const grantType = request.headers.token.grantType;
  const userData: Models.IUserData = request.headers.token.userData;
  if (!userData?.userLevel?.includes('USER') || grantType === 'client_credentials') {
    return (accountNo: string, fieldName: string) => createSuccessValidation(accountNo);
  }
  return validationAccountNoCreator(userData);
}

function isSubNumberNoValid(subNumber: string, tokenUserData: Models.IUserData, isRequire: boolean): boolean {
  if (!isRequire && Utils.isEmpty(subNumber)) {
    return true;
  }
  if (Utils.isEmpty(subNumber) || tokenUserData == null) {
    return false;
  }
  if (tokenUserData['bankInfo'] != null) {
    return Object.keys(tokenUserData['bankInfo']).includes(subNumber);
  }
  return false;
}

export function validateSubAccount<T extends Models.IDataRequest>(request: T, isRequire: boolean) {
  if (request.headers.token.userData == null) {
    return (subNumber: string, fieldName: string) =>
      createFailValidation('INVALID_FIELD_VALUE', [fieldName, subNumber], fieldName);
  }
  const userData: Models.IUserData = request.headers.token.userData;
  return (subNumber: string, fieldName: string) => {
    if (isSubNumberNoValid(subNumber, userData, isRequire)) {
      return createSuccessValidation(subNumber);
    } else {
      return createFailValidation('INVALID_FIELD_VALUE', [fieldName, subNumber], fieldName);
    }
  };
}

export function getPlatformValueCore(platform: string) {
  if (config.platform && Object.keys(config.platform).includes(platform)) {
    return config.platform[platform.toUpperCase()];
  }
  return config.defaultPlatform;
}

export async function getBankCode(
  accountNumber: string,
  subNumber: string,
  bankInfo: { [key: string]: string[] },
  accountBankInfoRepository: AccountBankInfoRepository
) {
  if (bankInfo != null && bankInfo[subNumber] != null) {
    return getElementAtIndex<string>(bankInfo[subNumber]);
  }

  const accountBankInfo: AccountBankInfo = await accountBankInfoRepository.findOne({
    username: accountNumber.toUpperCase(),
    subNumber,
  });

  return accountBankInfo ? accountBankInfo.bankCode : config.defaultBankCode;
}
