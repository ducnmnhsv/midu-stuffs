import { Errors, Models, Utils } from 'tradex-common';

const { InvalidParameterError } = Errors;
const { validate } = Utils;

export interface IRegisterLoanRequest extends Models.IDataRequest {
  items: IRegisterLoanRequestItem[];
}

export interface IRegisterLoanRequestItem {
  accountNumber: string;
  subNumber: string;
  settleBankCode: string;
  matchDate: string;
  settleDate: string;
  stockCode: string;
  matchQuantity: number;
  matchAmount: number;
  tradingFee: number;
  adjustAmount: number;
  possibleAmount: number;
  loanAmount: number;
  feeRate: number;
  tax: number;
  loanOrderType: string;
  loanBankCode: string;
}

export const validRequest = (request: IRegisterLoanRequest): void => {
  const error = new InvalidParameterError();
  validate(request.items, 'items')
    .setRequire()
    .throwValid(error);
  if (request.items == null || request.items.length < 1) {
    throw new InvalidParameterError();
  } else {
    for (let i = 0; i < request.items.length; i++) {
      const item: IRegisterLoanRequestItem = request.items[i];
      validate(item.accountNumber, `accountNumber[${i}]`)
        .setRequire()
        .throwValid(error);
      validate(item.loanBankCode, `loanBankCode[${i}]`)
        .setRequire()
        .throwValid(error);
      validate(item.matchDate, `matchDate[${i}]`)
        .setRequire()
        .throwValid(error);
      validate(item.settleDate, `settleDate[${i}]`)
        .setRequire()
        .throwValid(error);
      validate(item.stockCode, `stockCode[${i}]`)
        .setRequire()
        .throwValid(error);
      validate(item.matchQuantity, `matchQuantity[${i}]`)
        .setRequire()
        .throwValid(error);
      validate(item.matchAmount, `matchAmount[${i}]`)
        .setRequire()
        .throwValid(error);
      validate(item.tradingFee, `tradingFee[${i}]`)
        .setRequire()
        .throwValid(error);
      validate(item.adjustAmount, `adjustAmount[${i}]`)
        .setRequire()
        .throwValid(error);
      validate(item.possibleAmount, `possibleAmount[${i}]`)
        .setRequire()
        .throwValid(error);
      validate(item.loanAmount, `loanAmount[${i}]`)
        .setRequire()
        .throwValid(error);
      validate(item.tax, `tax[${i}]`)
        .setRequire()
        .throwValid(error);
      validate(item.loanOrderType, `loanOrderType[${i}]`)
        .setRequire()
        .throwValid(error);
    }
  }
  error.throwErr();
};
