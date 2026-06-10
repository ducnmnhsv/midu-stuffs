import { Inject, Service } from 'typedi';
import { LotteBalanceDao } from '../daos/LotteBalanceDao';
import { Errors, Utils } from 'tradex-common';
import { IContext } from '../models/IContext';
import { IParam } from '../models/request/lotte/ILotteRequest';
import { getElementAtIndex, parseMessages, setDefault, validateRequestAccountNoCreator } from '../utils/lotte';
import config from '../config';
import { Constants } from '../constants/Constants';
import { IRegisterLoanRequest, IRegisterLoanRequestItem, validRequest } from '../models/request/IRegisterLoanRequest';
import { ILotteRegisterLoanRequest } from '../models/request/lotte/ILotteRegisterLoanRequest';
import { ILotteRegisterLoanResponse } from '../models/response/lotte/ILotteRegisterLoanResponse';
import { ILoanAvailableRequest } from '../models/request/ILoanAvailableRequest';
import { ILotteLoanAvailableRequest } from '../models/request/lotte/ILotteLoanAvailableRequest';
import {
  ILotteLoanAvailableResponse,
  ILotteLoanAvailableResponseData,
} from '../models/response/lotte/ILotteLoanAvailableResponse';
import { ILoanAvailableResponse } from '../models/response/ILoanAvailableResponse';
import { ILoanHistoryRequest } from '../models/request/ILoanHistoryRequest';
import { ILotteLoanHistoryRequest } from '../models/request/lotte/ILotteLoanHistoryRequest';
import {
  ILotteLoanHistoryResponse,
  ILotteLoanHistoryResponseData,
} from '../models/response/lotte/ILotteLoanHistoryResponse';
import { ILoanHistoryResponse } from '../models/response/ILoanHistoryResponse';
import { ILoanDetailRequest } from '../models/request/ILoanDetailRequest';
import { ILoanDetailResponse } from '../models/response/ILoanDetailResponse';
import { ILotteLoanDetailRequest } from '../models/request/lotte/ILotteLoanDetailRequest';
import {
  ILotteLoanDetailResponse,
  ILotteLoanDetailResponseData,
} from '../models/response/lotte/ILotteLoanDetailResponse';
import { ILoanEstimatedFeeRequest } from '../models/request/ILoanEstimatedFeeRequest';
import { ILotteLoanEstimatedFeeRequest } from '../models/request/lotte/ILotteLoanEstimatedFeeRequest';
import {
  ILotteLoanEstimatedFeeResponse,
  ILotteLoanEstimatedFeeResponseData,
} from '../models/response/lotte/ILotteLoanEstimatedFeeResponse';
import { ILoanEstimatedFeeResponse } from '../models/response/ILoanEstimatedFeeResponse';

const { GeneralError, InvalidParameterError } = Errors;
const { validate } = Utils;

@Service()
export class LoanService {
  @Inject()
  private lotteBalanceDao: LotteBalanceDao;

  async registerLoan(request: IRegisterLoanRequest, ctx: IContext): Promise<IParam> {
    validRequest(request);
    const requestItem: IRegisterLoanRequestItem = getElementAtIndex<IRegisterLoanRequestItem>(request.items);
    const subNumber: string = setDefault<string>(requestItem.subNumber, config.defaultSubNumber);
    const bankInfo = request.headers.token.userData['bankInfo'];
    const bankCode: string = bankInfo != null ? getElementAtIndex(bankInfo[subNumber]) : config.defaultBankCode;
    const lotteRequest: ILotteRegisterLoanRequest = {
      hts_user_id: request.headers.token.userData.username,
      dept_no1: request.headers.token.userData.deptCode,
      idno: request.headers.token.userData.identifierNumber,
      acnt_no: requestItem.accountNumber.toUpperCase(),
      sub_no: subNumber,
      setl_bank_cd: setDefault<string>(
        requestItem.settleBankCode,
        setDefault<string>(bankCode, config.defaultBankCode)
      ),
      mth_dt: setDefault<string>(requestItem.matchDate, ''),
      setl_dt: setDefault<string>(requestItem.settleDate, ''),
      stk_cd: setDefault<string>(requestItem.stockCode, ''),
      mrtg_lnd_qty: `${requestItem.matchQuantity}`,
      mth_amt: `${requestItem.matchAmount}`,
      mth_cmsn: `${requestItem.tradingFee}`,
      adj_amt: `${requestItem.adjustAmount}`,
      lnd_abl_amt: `${requestItem.possibleAmount}`,
      lnd_amt: `${requestItem.loanAmount}`,
      lnd_rt: `${requestItem.feeRate}`,
      sb_tax: `${requestItem.tax}`,
      cdt_tp: `${requestItem.loanOrderType}`,
    };
    const lotteRes: ILotteRegisterLoanResponse = await this.lotteBalanceDao.registerLoan(lotteRequest, ctx);
    const { codes, messages } = parseMessages(lotteRes.error_desc, lotteRes.error_code);
    if (codes === null || codes === '0406') {
      return {
        message: messages,
      };
    } else {
      throw new GeneralError(`${Constants.LOAN_REGISTER}${messages}`);
    }
  }

  async loanAvailable(request: ILoanAvailableRequest, ctx: IContext): Promise<ILoanAvailableResponse[]> {
    const error = new InvalidParameterError();
    validate(request.accountNumber, 'accountNumber')
      .setRequire()
      .add(validateRequestAccountNoCreator(request))
      .throwValid(error);
    error.throwErr();
    const subNumber: string = setDefault<string>(request.subNumber, config.defaultSubNumber);
    const bankInfo = request.headers.token.userData['bankInfo'];
    const bankCode: string = bankInfo != null ? getElementAtIndex(bankInfo[subNumber]) : config.defaultBankCode;
    const nextData: string =
      (request.lastSettleBankCode || bankCode) +
      setDefault<string>(request.lastMatchDate, '00000000') +
      setDefault<string>(request.lastSettleDate, '00000000') +
      setDefault<string>(request.lastLoanOrderType, '00');
    const lotteRequest: ILotteLoanAvailableRequest = {
      acnt_no: request.accountNumber.toUpperCase(),
      sub_no: subNumber,
      next_data: nextData,
      row_count: setDefault<number>(request.fetchCount, config.defaultFetchCount).toString(),
    };
    const lotteRes: ILotteLoanAvailableResponse = await this.lotteBalanceDao.loanAvailable(lotteRequest, ctx);
    const { codes, messages } = parseMessages(lotteRes.error_desc, lotteRes.error_code);
    if (codes === null || codes === '0011') {
      return lotteRes.data_list.map(
        (item: ILotteLoanAvailableResponseData): ILoanAvailableResponse => ({
          matchDate: item.mth_dt,
          settleDate: item.setl_dt,
          matchAmount: Number(item.sb_amt),
          tradingFee: Number(item.sb_cmsn),
          tax: Number(item.sb_tax),
          adjustAmount: Number(item.adj_amt),
          loanPeriod: Number(item.lnd_prd),
          feeRate: Number(item.lnd_cmsn_rt),
          estimatedFee: Number(item.estm_cmsn),
          possibleAmount: Number(item.lnd_abl_amt),
          loanBankName: item.bank_nm,
          loanOrderName: item.cdt_nm,
          settleBankCode: item.bank_cd,
          loanOrderType: item.cdt_tp,
        })
      );
    } else if (codes !== null && codes === '2016') {
      return [];
    } else {
      throw new GeneralError(`${Constants.LOAN_AVAILABLE}${messages}`);
    }
  }

  async loanHistory(request: ILoanHistoryRequest, ctx: IContext): Promise<ILoanHistoryResponse[]> {
    const error = new InvalidParameterError();
    validate(request.accountNumber, 'accountNumber')
      .setRequire()
      .add(validateRequestAccountNoCreator(request))
      .throwValid(error);
    error.throwErr();
    const subNumber: string = setDefault<string>(request.subNumber, config.defaultSubNumber);
    const bankInfo = request.headers.token.userData['bankInfo'];
    const bankCode: string = bankInfo != null ? getElementAtIndex(bankInfo[subNumber]) : config.defaultBankCode;
    const nextData: string =
      setDefault<string>(request.lastLoanDate, '').padStart(8, '9') +
      setDefault<string>(request.lastLoanBankCode, setDefault<string>(bankCode, config.defaultBankCode)).padStart(
        4,
        '0'
      ) +
      setDefault<string>(request.lastMatchDate, '').padStart(8, '0') +
      setDefault<string>(request.lastStockCode, '').padStart(20, '0');
    const lotteRequest: ILotteLoanHistoryRequest = {
      acnt_no: request.accountNumber.toUpperCase(),
      sub_no: subNumber,
      next_data: nextData,
      row_count: setDefault<number>(request.fetchCount, config.defaultFetchCount).toString(),
    };
    const lotteRes: ILotteLoanHistoryResponse = await this.lotteBalanceDao.loanHistory(lotteRequest, ctx);
    const { codes, messages } = parseMessages(lotteRes.error_desc, lotteRes.error_code);
    if (codes === null || codes === '0011') {
      return lotteRes.data_list.map(
        (item: ILotteLoanHistoryResponseData): ILoanHistoryResponse => ({
          loanDate: item.lnd_dt,
          matchDate: item.mth_dt,
          stockCode: item.stk_cd,
          matchQuantity: Number(item.mrtg_lnd_qty),
          matchAmount: Number(item.mth_amt),
          loanAmount: Number(item.lnd_amt),
          loanRepayAmount: Number(item.lnd_rpy_amt),
          loanRemainAmount: Number(item.lnd_rm_amt),
          status: item.lnd_proc_stat,
          loanBankCode: item.lnd_bank_cd,
          loanBankName: item.lnd_bank_nm,
        })
      );
    } else if (codes !== null && codes === '2016') {
      return [];
    } else {
      throw new GeneralError(`${Constants.LOAN_AVAILABLE}${messages}`);
    }
  }

  async loanDetail(request: ILoanDetailRequest, ctx: IContext): Promise<ILoanDetailResponse[]> {
    const error = new InvalidParameterError();
    validate(request.accountNumber, 'accountNumber')
      .setRequire()
      .add(validateRequestAccountNoCreator(request))
      .throwValid(error);
    validate(request.matchDate, 'matchDate')
      .setRequire()
      .throwValid(error);
    validate(request.settleDate, 'settleDate')
      .setRequire()
      .throwValid(error);
    validate(request.loanOrderType, 'loanOrderType')
      .setRequire()
      .throwValid(error);
    validate(request.loanBankCode, 'loanBankCode')
      .setRequire()
      .throwValid(error);
    error.throwErr();
    const subNumber: string = setDefault<string>(request.subNumber, config.defaultSubNumber);
    const bankInfo = request.headers.token.userData['bankInfo'];
    const bankCode: string = bankInfo != null ? getElementAtIndex(bankInfo[subNumber]) : config.defaultBankCode;
    const nextData: string =
      setDefault<string>(request.lastSettleBankCode, setDefault<string>(bankCode, config.defaultBankCode)).padStart(
        4,
        '9'
      ) +
      setDefault<string>(request.lastStockCode, '').padStart(20, '0') +
      setDefault<string>(request.lastLoanOrderType, '').padStart(2, '0');
    const lotteRequest: ILotteLoanDetailRequest = {
      acnt_no: request.accountNumber.toUpperCase(),
      sub_no: subNumber,
      mth_dt: request.matchDate,
      setl_dt: request.settleDate,
      cdt_tp: request.loanOrderType,
      setl_bank_cd: setDefault<string>(request.settleBankCode, config.defaultBankCode),
      next_data: nextData,
      row_count: setDefault<number>(request.fetchCount, config.defaultFetchCount).toString(),
    };
    const lotteRes: ILotteLoanDetailResponse = await this.lotteBalanceDao.loanDetail(lotteRequest, ctx);
    const { codes, messages } = parseMessages(lotteRes.error_desc, lotteRes.error_code);
    if (codes === null || codes === '0011') {
      return lotteRes.data_list.map(
        (item: ILotteLoanDetailResponseData): ILoanDetailResponse => ({
          matchDate: item.mth_dt,
          settleDate: item.setl_dt,
          stockCode: item.stk_cd,
          matchQuantity: Number(item.sb_qty),
          matchAmount: Number(item.sb_amt),
          tradingFee: Number(item.sb_cmsn),
          tax: Number(item.sb_tax),
          adjustAmount: Number(item.adj_amt),
          possibleAmount: Number(item.lnd_abl_amt),
          settleBankName: item.bank_nm,
          settleBankCode: item.bank_cd,
          loanOrderType: item.cdt_tp,
        })
      );
    } else if (codes !== null && codes === '2016') {
      return [];
    } else {
      throw new GeneralError(`${Constants.LOAN_AVAILABLE}${messages}`);
    }
  }

  async loanEstimatedFee(request: ILoanEstimatedFeeRequest, ctx: IContext): Promise<ILoanEstimatedFeeResponse> {
    const error = new InvalidParameterError();
    validate(request.accountNumber, 'accountNumber')
      .setRequire()
      .add(validateRequestAccountNoCreator(request))
      .throwValid(error);
    validate(request.subNumber, 'subNumber')
      .setRequire()
      .throwValid(error);
    validate(request.loanBankCode, 'loanBankCode')
      .setRequire()
      .throwValid(error);
    validate(request.settleDate, 'settleDate')
      .setRequire()
      .throwValid(error);
    validate(request.amount, 'amount')
      .setRequire()
      .throwValid(error);
    error.throwErr();
    const lotteRequest: ILotteLoanEstimatedFeeRequest = {
      acnt_no: request.accountNumber,
      sub_no: request.subNumber,
      lnd_bank_cd: request.loanBankCode,
      rpy_dt: request.settleDate,
      amt: request.amount,
    };
    const lotteRes: ILotteLoanEstimatedFeeResponse = await this.lotteBalanceDao.loanEstimatedFee(lotteRequest, ctx);
    const { codes } = parseMessages(lotteRes.error_desc, lotteRes.error_code);
    if (codes === null) {
      const lotteResDataList: ILotteLoanEstimatedFeeResponseData = getElementAtIndex<
        ILotteLoanEstimatedFeeResponseData
      >(lotteRes.data_list);
      return {
        estimatedFee: Number(lotteResDataList.adv_payment_fee),
      };
    } else {
      throw new GeneralError(lotteRes.error_desc);
    }
  }
}
