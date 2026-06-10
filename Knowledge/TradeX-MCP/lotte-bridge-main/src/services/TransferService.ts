import { Inject, Service } from 'typedi';
import { LotteBalanceDao } from '../daos/LotteBalanceDao';
import { Errors, Utils } from 'tradex-common';
import { IContext } from '../models/IContext';
import { IParam } from '../models/request/lotte/ILotteRequest';
import { parseMessages, setDefault, validateRequestAccountNoCreator } from '../utils/lotte';
import config from '../config';
import { Constants } from '../constants/Constants';
import { ITransferCashRequest } from '../models/request/ITransferCashRequest';
import { ITransferStockRequest } from '../models/request/ITransferStockRequest';
import { ILotteTransferCashRequest } from '../models/request/lotte/ILotteTransferCashRequest';
import { ILotteTransferStockRequest } from '../models/request/lotte/ILotteTransferStockRequest';
import { ILotteTransferCashResponse } from '../models/response/lotte/ILotteTransferCashResponse';
import { ILotteTransferStockResponse } from '../models/response/lotte/ILotteTransferStockResponse';
import { ITransferStockBalanceRequest } from '../models/response/lotte/ITransferStockBalanceRequest';
import {
  ILotteTransferStockBalanceResponse,
  ILotteTransferStockBalanceResponseData,
} from '../models/response/lotte/ILotteTransferStockBalanceResponse';
import { ITransferStockBalanceResponse } from '../models/response/ITransferStockBalanceResponse';
import { ITransferCashHistoryRequest, TransferCashHistoryStatus } from '../models/request/ITransferCashHistoryRequest';
import { ITransferStockHistoryRequest } from '../models/request/ITransferStockHistoryRequest';
import { ITransferCashHistoryResponse } from '../models/response/ITransferCashHistoryResponse';
import { ILotteTransferStockHistoryRequest } from '../models/request/lotte/ILotteTransferStockHistoryRequest';
import {
  ILotteTransferStockHistoryResponse,
  ILotteTransferStockHistoryResponseData,
} from '../models/response/lotte/ILotteTransferStockHistoryResponse';
import { ILotteTransferCashHistoryRequest } from '../models/request/lotte/ILotteTransferCashHistoryRequest';
import {
  ILotteTransferCashHistoryResponse,
  ILotteTransferCashHistoryResponseData,
} from '../models/response/lotte/ILotteTransferCashHistoryResponse';
import { ITransferStockHistoryResponse } from '../models/response/ITransferStockHistoryResponse';
import { ILotteTransferStockBalanceRequest } from '../models/request/lotte/ILotteTransferStockBalanceRequest';
import { CONFIRM_STATUS, LOTTE_LANG_CODE } from '../constants/enum';
import { ITransferCashConfirmRequest } from '../models/request/ITransferCashConfirmRequest';
import { ITransferCashConfirmResponse } from '../models/response/ITransferCashConfirmResponse';
import { ILotteTransferCashConfirmRequest } from '../models/request/lotte/ILotteTransferCashConfirmRequest';
import { ILoanConfirmRequest } from '../models/request/ILoanConfirmRequest';
import { ILoanConfirmResponse } from '../models/response/ILoanConfirmResponse';
import { IGetLoanConfirmRequest } from '../models/request/IGetLoanConfirmRequest';
import { IGetLoanConfirmResponse } from '../models/response/IGetLoanConfirmResponse';
import { IGetTransferCashConfirmRequest } from '../models/request/IGetTransferCashConfirmRequest';
import { IGetTransferCashConfirmResponse } from '../models/response/IGetTransferCashConfirmResponse';
import { ILotteTransferCashConfirmResponse } from '../models/response/lotte/ILotteTransferCashConfirmResponse';
import { ILotteGetTransferCashConfirmRequest } from '../models/request/lotte/ILotteGetTransferCashConfirmRequest';
import {
  ILotteGetTransferCashConfirmResponse,
  ILotteGetTransferCashConfirmResponseData,
} from '../models/response/lotte/ILotteGetTransferCashConfirmResponse';
import { ILotteLoanConfirmRequest } from '../models/request/lotte/ILotteLoanConfirmRequest';
import { ILotteLoanConfirmResponse } from '../models/response/lotte/ILotteLoanConfirmResponse';
import { ILotteGetLoanConfirmRequest } from '../models/request/lotte/ILotteGetLoanConfirmRequest';
import {
  ILotteGetLoanConfirmResponse,
  ILotteGetLoanConfirmResponseData,
} from '../models/response/lotte/ILotteGetLoanConfirmResponse';
import { ITransferStockConfirmRequest } from '../models/request/ITransferStockConfirmRequest';
import { ITransferStockConfirmResponse } from '../models/response/ITransferStockConfirmResponse';
import { ILotteTransferStockConfirmRequest } from '../models/request/lotte/ILotteTransferStockConfirmRequest';
import { ILotteTransferStockConfirmResponse } from '../models/response/lotte/ILotteTransferStockConfirmResponse';
import { IGetTransferStockConfirmResponse } from '../models/response/IGetTransferStockConfirmResponse';
import { IGetTransferStockConfirmRequest } from '../models/request/IGetTransferStockConfirmRequest';
import { ILotteGetTransferStockConfirmRequest } from '../models/request/lotte/ILotteGetTransferStockConfirmRequest';
import {
  ILotteGetTransferStockConfirmResponse,
  ILotteGetTransferStockConfirmResponseData,
} from '../models/response/lotte/ILotteGetTransferStockConfirmResponse';

const { GeneralError, InvalidParameterError } = Errors;
const { validate, formatDateToDisplay, convertStringToDate, DATE_DISPLAY_FORMAT } = Utils;
const DATE_DISPLAY_FORMAT_LT = 'DDMMYYYY';

@Service()
export class TransferService {
  @Inject()
  private lotteBalanceDao: LotteBalanceDao;

  async transferCash(request: ITransferCashRequest, ctx: IContext): Promise<IParam> {
    const error = new InvalidParameterError();
    validate(request.accountNumber, 'accountNumber')
      .setRequire()
      .add(validateRequestAccountNoCreator(request))
      .throwValid(error);
    validate(request.receivedAccountNumber, 'receivedAccountNumber')
      .setRequire()
      .throwValid(error);
    validate(request.receivedSubNumber, 'receivedSubNumber')
      .setRequire()
      .throwValid(error);
    validate(request.amount, 'amount')
      .setRequire()
      .throwValid(error);
    validate(request.note, 'note')
      .setRequire()
      .throwValid(error);
    error.throwErr();
    const language = ctx.orgMsg.data['accept-language'];
    const lotteRequest: ILotteTransferCashRequest = {
      hts_user_id: request.headers.token.userData.username,
      lang_code:
        language == null || LOTTE_LANG_CODE[language] == null ? config.defaultLanguage : LOTTE_LANG_CODE[language],
      acnt_no: request.accountNumber.toUpperCase(),
      sub_no: setDefault<string>(request.subNumber, config.defaultSubNumber),
      amount: request.amount,
      recv_sub: request.receivedSubNumber,
      remark: setDefault<string>(request.note, ''),
    };
    const lotteRes: ILotteTransferCashResponse = await this.lotteBalanceDao.tranferCash(lotteRequest, ctx);
    const { codes, messages } = parseMessages(lotteRes.error_desc, lotteRes.error_code);
    if (codes === null || codes === '0012' || codes === '0010') {
      return {
        message: messages,
      };
    } else {
      throw new GeneralError(`${Constants.TRANSFER_CASH}${messages}`);
    }
  }

  async transferStock(request: ITransferStockRequest, ctx: IContext): Promise<IParam> {
    const error = new InvalidParameterError();
    validate(request.accountNumber, 'accountNumber')
      .setRequire()
      .add(validateRequestAccountNoCreator(request))
      .throwValid(error);
    validate(request.receivedAccountNumber, 'receivedAccountNumber')
      .setRequire()
      .throwValid(error);
    validate(request.receivedSubNumber, 'receivedSubNumber')
      .setRequire()
      .throwValid(error);
    validate(request.stockCode, 'stockCode')
      .setRequire()
      .throwValid(error);
    validate(request.quantity, 'quantity')
      .setRequire()
      .throwValid(error);
    validate(request.note, 'note')
      .setRequire()
      .throwValid(error);
    validate(request.limitedQuantity, 'limitedQuantity')
      .setRequire()
      .throwValid();
    error.throwErr();
    const lotteRequest: ILotteTransferStockRequest = {
      hts_user_id: request.headers.token.userData.username,
      acnt_no: request.accountNumber.toUpperCase(),
      sub_no: setDefault<string>(request.subNumber, config.defaultSubNumber),
      stk_cd: request.stockCode,
      acnt_r: request.accountNumber.toUpperCase(),
      sub_r: request.receivedSubNumber,
      qty: request.quantity,
      lmt_qty: request.limitedQuantity,
      cnte: setDefault<string>(request.note, ''),
    };
    const lotteRes: ILotteTransferStockResponse = await this.lotteBalanceDao.transferStock(lotteRequest, ctx);
    const { codes, messages } = parseMessages(lotteRes.error_desc, lotteRes.error_code);
    if (codes === null || codes === '0012' || codes === '0405') {
      return {
        message: messages,
      };
    } else {
      throw new GeneralError(`${Constants.TRANSFER_STOCK}${messages}`);
    }
  }

  async getTransferStockBalance(
    request: ITransferStockBalanceRequest,
    ctx: IContext
  ): Promise<ITransferStockBalanceResponse[]> {
    const error = new InvalidParameterError();
    validate(request.accountNumber, 'accountNumber')
      .setRequire()
      .add(validateRequestAccountNoCreator(request))
      .throwValid(error);
    error.throwErr();
    const lotteRequest: ILotteTransferStockBalanceRequest = {
      acnt_no: request.accountNumber.toUpperCase(),
      sub_no: setDefault<string>(request.subNumber, config.defaultSubNumber),
      next_key: setDefault<string>(request.lastStockCode, config.defaultNextKeyAscOrder),
      row_count: setDefault<number>(request.fetchCount, config.defaultFetchCount).toString(),
    };
    const lotteRes: ILotteTransferStockBalanceResponse = await this.lotteBalanceDao.getTransferStockBalance(
      lotteRequest,
      ctx
    );
    const { codes, messages } = parseMessages(lotteRes.error_desc, lotteRes.error_code);
    if (codes === null || codes === '0011') {
      return lotteRes.data_list.map(
        (item: ILotteTransferStockBalanceResponseData): ITransferStockBalanceResponse => ({
          stockCode: item.stk_cd,
          stockName: item.stk_mn,
          availableQuantity: Number(item.able_qty),
          limitAvailableQuantity: Number(item.able_limt_qty),
        })
      );
    } else if (codes !== null || codes === '2016') {
      return [];
    } else {
      throw new GeneralError(`${Constants.TRANSFER_STOCK_BALANCE}${messages}`);
    }
  }

  async getTransferStockHistory(
    request: ITransferStockHistoryRequest,
    ctx: IContext
  ): Promise<ITransferStockHistoryResponse[]> {
    const grantType: string = request.headers?.token?.grantType;
    const error = new InvalidParameterError();
    validate(request.accountNumber, 'accountNumber')
      .setRequire()
      .add(validateRequestAccountNoCreator(request))
      .throwValid(error);
    if (grantType !== 'client_credentials') {
      validate(request.branchCode, 'branchCode')
        .setRequire()
        .throwValid(error);
    }
    error.throwErr();
    const nextKey: string =
      setDefault<string>(request.lastTransactionDate, '').padStart(8, '0') +
      setDefault<string>(request.lastReceivedAccountNumber, '').padStart(10, '0') +
      setDefault<number>(request.lastSequenceNumber, 0)
        .toString()
        .padStart(3, '0');
    const now: string = formatDateToDisplay(new Date(), DATE_DISPLAY_FORMAT);
    const lotteRequest: ILotteTransferStockHistoryRequest = {
      acnt_no: request.accountNumber.toUpperCase(),
      sub_no: setDefault<string>(request.subNumber, config.defaultSubNumber),
      date_fr: setDefault<string>(request.fromDate, config.defaultFromDate),
      date_to: setDefault<string>(request.toDate, now),
      next_key: nextKey,
      row_count: setDefault<number>(request.fetchCount, config.defaultFetchCount).toString(),
    };
    const lotteRes: ILotteTransferStockHistoryResponse = await this.lotteBalanceDao.getTransferStockHistory(
      lotteRequest,
      ctx
    );
    const { codes, messages } = parseMessages(lotteRes.error_desc, lotteRes.error_code);
    if (codes === null || codes === '0011') {
      return lotteRes.data_list.map(
        (item: ILotteTransferStockHistoryResponseData): ITransferStockHistoryResponse => ({
          transactionDate: formatDateToDisplay(
            convertStringToDate(item.proc_dt, DATE_DISPLAY_FORMAT_LT),
            DATE_DISPLAY_FORMAT
          ),
          sequenceNumber: item.seq_no,
          receivedAccountNumber: item.acnt_no_r,
          receivedSubNumber: item.sub_no_r,
          stockCode: item.stk_cd,
          quantity: Number(item.qty),
          limitedQuantity: Number(item.sb_lmt_qty),
          note: item.cnte,
        })
      );
    } else if (codes !== null && codes === '2016') {
      return [];
    } else {
      throw new GeneralError(`${Constants.TRANSFER_STOCK_HISTORY}${messages}`);
    }
  }

  async getTransferCashHistory(
    request: ITransferCashHistoryRequest,
    ctx: IContext
  ): Promise<ITransferCashHistoryResponse[]> {
    const error = new InvalidParameterError();
    validate(request.accountNumber, 'accountNumber')
      .setRequire()
      .add(validateRequestAccountNoCreator(request))
      .throwValid(error);
    validate(request.status, 'status')
      .setRequire()
      .throwValid(error);
    error.throwErr();
    if (request.status !== TransferCashHistoryStatus.APPROVED_INTERNAL) {
      return [];
    }
    const lotteRequest: ILotteTransferCashHistoryRequest = {
      acnt_no: request.accountNumber.toUpperCase(),
      sub_no: setDefault<string>(request.subNumber, config.defaultSubNumber),
      from_date: setDefault<string>(request.fromDate, config.defaultFromDate),
      to_date: setDefault<string>(request.toDate, formatDateToDisplay(new Date(), DATE_DISPLAY_FORMAT)),
      next_key: setDefault<string>(request.next, config.defaultNextKeyDesc),
      row_count: setDefault<number>(request.fetchCount, config.defaultFetchCount).toString(),
    };
    const lotteRes: ILotteTransferCashHistoryResponse = await this.lotteBalanceDao.getTransferCashHistory(
      lotteRequest,
      ctx
    );
    const { codes, messages } = parseMessages(lotteRes.error_desc, lotteRes.error_code);
    if (codes === null || codes === '0011') {
      return lotteRes.data_list.map(
        (item: ILotteTransferCashHistoryResponseData): ITransferCashHistoryResponse => ({
          transactionDate: item.date,
          sequenceNumber: Number(item.from_seq),
          accountNumber: item.from_account,
          subNumber: item.from_sub,
          amount: Number(item.amount),
          receivedAccountName: item.to_account,
          receivedSubNumber: item.to_sub,
          note: item.remark,
          isCancel: item.cancel_yn === 'Y',
          next: item.next_key,
        })
      );
    } else if (codes !== null && codes === '2016') {
      return [];
    } else {
      throw new GeneralError(`${Constants.TRANSFER_CASH_HISTORY}${messages}`);
    }
  }

  async transferCashConfirm(
    request: ITransferCashConfirmRequest,
    ctx: IContext
  ): Promise<ITransferCashConfirmResponse> {
    const error = new InvalidParameterError();
    validate(request.accountNumber, 'accountNumber')
      .setRequire()
      .add(validateRequestAccountNoCreator(request))
      .throwValid(error);
    validate(request.date, 'date')
      .setRequire()
      .throwValid(error);
    validate(request.subNumber, 'subNumber')
      .setRequire()
      .throwValid(error);
    validate(request.sequenceNumber, 'sequenceNumber')
      .setRequire()
      .throwValid(error);
    error.throwErr();
    const lotteRequest: ILotteTransferCashConfirmRequest = {
      acnt_no: request.accountNumber.toUpperCase(),
      date: request.date,
      sub_no: request.subNumber,
      seq_no: request.sequenceNumber,
    };
    const lotteRes: ILotteTransferCashConfirmResponse = await this.lotteBalanceDao.cashTransferConfirm(
      lotteRequest,
      ctx
    );
    const { codes } = parseMessages(lotteRes.error_desc, lotteRes.error_code);
    if (codes === null || codes === '0806') {
      return {
        message: lotteRes.error_desc,
      };
    } else {
      throw new GeneralError(lotteRes.error_desc);
    }
  }

  async getTransferCashConfirm(
    request: IGetTransferCashConfirmRequest,
    ctx: IContext
  ): Promise<IGetTransferCashConfirmResponse[]> {
    const error = new InvalidParameterError();
    validate(request.accountNumber, 'accountNumber')
      .setRequire()
      .add(validateRequestAccountNoCreator(request))
      .throwValid(error);
    validate(request.subNumber, 'subNumber')
      .setRequire()
      .throwValid(error);
    validate(request.fromDate, 'fromDate')
      .setRequire()
      .throwValid(error);
    validate(request.toDate, 'toDate')
      .setRequire()
      .throwValid(error);
    error.throwErr();
    const lotteRequest: ILotteGetTransferCashConfirmRequest = {
      trans_sign:
        request.status != null
          ? setDefault<string>(CONFIRM_STATUS[request.status], CONFIRM_STATUS.ALL)
          : CONFIRM_STATUS.ALL,
      from_dt: setDefault<string>(request.fromDate, 'D'), // https://difisoft.atlassian.net/browse/NHSV-1217?focusedCommentId=14754
      to_dt: setDefault<string>(request.toDate, 'D'),
      acnt_no: request.accountNumber.toUpperCase(),
      sub_no: request.subNumber,
      next: setDefault<string>(request.nextKey, '0'),
      hts_user_id: request.headers.token.userData.username,
      row_count: setDefault<number>(request.fetchCount, config.defaultFetchCount).toString(),
    };
    const lotteRes: ILotteGetTransferCashConfirmResponse = await this.lotteBalanceDao.getCashTransferConfirm(
      lotteRequest,
      ctx
    );
    const { codes } = parseMessages(lotteRes.error_desc, lotteRes.error_code);
    if (codes === null) {
      return lotteRes.data_list.map(
        (it: ILotteGetTransferCashConfirmResponseData): IGetTransferCashConfirmResponse => ({
          status: it.trans_sign,
          transactionDate: it.std_dt,
          sequenceNumber: it.seq_no,
          amount: Number(it.trd_amt),
          receivedAccountNumber: it.inamt_acnt_no,
          receivedSubNumber: it.inamt_sub_no,
          channel: it.mdm_tp,
          isCancel: it.cncl_yn === 'Y',
          note: it.cnte,
          nextKey: it.next,
        })
      );
    } else {
      throw new GeneralError(lotteRes.error_desc);
    }
  }

  async loanConfirm(request: ILoanConfirmRequest, ctx: IContext): Promise<ILoanConfirmResponse> {
    const error = new InvalidParameterError();
    validate(request.accountNumber, 'accountNumber')
      .setRequire()
      .add(validateRequestAccountNoCreator(request))
      .throwValid(error);
    validate(request.matchDate, 'matchDate')
      .setRequire()
      .throwValid(error);
    validate(request.subNumber, 'subNumber')
      .setRequire()
      .throwValid(error);
    validate(request.sequenceNumber, 'sequenceNumber')
      .setRequire()
      .throwValid(error);
    validate(request.username, 'username')
      .setRequire()
      .throwValid(error);
    validate(request.loanBankCode, 'loanBankCode')
      .setRequire()
      .throwValid(error);
    validate(request.loanDate, 'loanDate')
      .setRequire()
      .throwValid(error);
    error.throwErr();
    const lotteRequest: ILotteLoanConfirmRequest = {
      acnt_no: request.accountNumber.toUpperCase(),
      sub_no: request.subNumber,
      lnd_dt: request.loanDate,
      mth_dt: request.matchDate,
      lnd_cntr_no: request.sequenceNumber,
      lnd_amt: request.loanAmount,
      work_mn: request.username,
      lnd_bank_cd: request.loanBankCode,
    };
    const lotteRes: ILotteLoanConfirmResponse = await this.lotteBalanceDao.loanConfirm(lotteRequest, ctx);
    const { codes } = parseMessages(lotteRes.error_desc, lotteRes.error_code);
    if (codes === null || codes === '0806') {
      return {
        message: lotteRes.error_desc,
      };
    } else {
      throw new GeneralError(lotteRes.error_desc);
    }
  }

  async getloanConfirm(request: IGetLoanConfirmRequest, ctx: IContext): Promise<IGetLoanConfirmResponse[]> {
    const error = new InvalidParameterError();
    validate(request.accountNumber, 'accountNumber')
      .setRequire()
      .add(validateRequestAccountNoCreator(request))
      .throwValid(error);
    validate(request.subNumber, 'subNumber')
      .setRequire()
      .throwValid(error);
    validate(request.fromDate, 'fromDate')
      .setRequire()
      .throwValid(error);
    validate(request.toDate, 'toDate')
      .setRequire()
      .throwValid(error);
    error.throwErr();
    const lotteRequest: ILotteGetLoanConfirmRequest = {
      lnd_sign:
        request.status != null
          ? setDefault<string>(CONFIRM_STATUS[request.status], CONFIRM_STATUS.ALL)
          : CONFIRM_STATUS.ALL,
      from_dt: setDefault<string>(request.fromDate, 'D'),
      end_dt: setDefault<string>(request.toDate, 'D'),
      acnt_no: request.accountNumber.toUpperCase(),
      sub_no: request.subNumber,
      next_data: setDefault<string>(request.nextKey, '0'),
      stk_cd: setDefault<string>(request.stockCode, '%'),
      hts_user_id: request.headers.token.userData.username,
      row_count: setDefault<number>(request.fetchCount, config.defaultFetchCount).toString(),
    };
    const lotteRes: ILotteGetLoanConfirmResponse = await this.lotteBalanceDao.getLoanConfirm(lotteRequest, ctx);
    const { codes } = parseMessages(lotteRes.error_desc, lotteRes.error_code);
    if (codes === null) {
      return lotteRes.data_list.map(
        (it: ILotteGetLoanConfirmResponseData): IGetLoanConfirmResponse => ({
          status: it.lnd_sign,
          loanDate: it.lnd_dt,
          matchDate: it.mth_dt,
          sequenceNumber: it.lnd_cntr_no,
          stockCode: it.stk_cd,
          loanPeriod: Number(it.day_cnt),
          feeRate: Number(it.lnd_cmsn_rt),
          amount: Number(it.lnd_amt),
          channel: it.work_mn,
          loanBankCode: it.lnd_bank_cd,
          nextKey: it.next_data,
        })
      );
    } else {
      throw new GeneralError(lotteRes.error_desc);
    }
  }

  async transferStockConfirm(
    request: ITransferStockConfirmRequest,
    ctx: IContext
  ): Promise<ITransferStockConfirmResponse> {
    const error = new InvalidParameterError();
    validate(request.accountNumber, 'accountNumber')
      .setRequire()
      .add(validateRequestAccountNoCreator(request))
      .throwValid(error);
    validate(request.date, 'date')
      .setRequire()
      .throwValid(error);
    validate(request.stockCode, 'stockCode')
      .setRequire()
      .throwValid(error);
    validate(request.sequenceNumber, 'sequenceNumber')
      .setRequire()
      .throwValid(error);
    error.throwErr();
    const lotteRequest: ILotteTransferStockConfirmRequest = {
      hts_user_id: request.headers.token.userData.username,
      acnt_no: request.accountNumber.toUpperCase(),
      date: request.date,
      stk_cd: request.stockCode,
      seq_no: request.sequenceNumber,
    };
    const lotteRes: ILotteTransferStockConfirmResponse = await this.lotteBalanceDao.transferStockConfirm(
      lotteRequest,
      ctx
    );
    const { codes } = parseMessages(lotteRes.error_desc, lotteRes.error_code);
    if (codes === null || codes === '0010') {
      return {
        message: lotteRes.error_desc,
      };
    } else {
      throw new GeneralError(lotteRes.error_desc);
    }
  }

  async getTransferStockConfirm(
    request: IGetTransferStockConfirmRequest,
    ctx: IContext
  ): Promise<IGetTransferStockConfirmResponse[]> {
    const error = new InvalidParameterError();
    validate(request.accountNumber, 'accountNumber')
      .setRequire()
      .add(validateRequestAccountNoCreator(request))
      .throwValid(error);
    validate(request.subNumber, 'subNumber')
      .setRequire()
      .throwValid(error);
    validate(request.fromDate, 'fromDate')
      .setRequire()
      .throwValid(error);
    validate(request.toDate, 'toDate')
      .setRequire()
      .throwValid(error);
    error.throwErr();
    const lotteRequest: ILotteGetTransferStockConfirmRequest = {
      status:
        request.status != null
          ? setDefault<string>(CONFIRM_STATUS[request.status], CONFIRM_STATUS.ALL)
          : CONFIRM_STATUS.ALL,
      from_dt: setDefault<string>(request.fromDate, 'D'),
      to_dt: setDefault<string>(request.toDate, 'D'),
      acnt_no: request.accountNumber.toUpperCase(),
      sub_no: request.subNumber,
      next: setDefault<string>(request.nextKey, '00000000000000000000'),
      stk_cd: setDefault<string>(request.stockCode, '%'),
      hts_user_id: request.headers.token.userData.username,
      row_count: setDefault<number>(request.fetchCount, config.defaultFetchCount).toString(),
    };
    const lotteRes: ILotteGetTransferStockConfirmResponse = await this.lotteBalanceDao.getTransferStockConfirm(
      lotteRequest,
      ctx
    );
    const { codes } = parseMessages(lotteRes.error_desc, lotteRes.error_code);
    if (codes === null) {
      return lotteRes.data_list.map(
        (it: ILotteGetTransferStockConfirmResponseData): IGetTransferStockConfirmResponse => ({
          status: it.status,
          transactionDate: it.proc_dt,
          sequenceNumber: it.seq_no,
          receivedAccountNumber: it.acnt_no_r,
          receivedSubNumber: it.sub_no_r,
          stockCode: it.stk_cd,
          quantity: Number(it.qty),
          limitedQuantity: Number(it.sb_lmt_qty),
          note: it.cnte,
          nextKey: it.next,
        })
      );
    } else {
      throw new GeneralError(lotteRes.error_desc);
    }
  }
}
