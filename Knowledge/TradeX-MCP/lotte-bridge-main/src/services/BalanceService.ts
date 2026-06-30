import { Inject, Service } from 'typedi';
import { LotteBalanceDao } from '../daos/LotteBalanceDao';
import { IProfitLossHistoryRequest } from '../models/request/IProfitLossHistoryRequest';
import { IContext } from '../models/IContext';
import {
  IProfitLossHistoryResponse,
  IProfitLossHistoryResponseItem,
} from '../models/response/IProfitLossHistoryResponse';
import { IParam } from '../models/request/lotte/ILotteRequest';
import {
  ILotteProfitLossHistoryResponse,
  ILotteProfitLossHistoryResponseData,
  ILotteProfitLossHistoryResponseDataItem,
} from '../models/response/lotte/ILotteProfitLossHistoryResponse';
import {
  getBankCode,
  getElementAtIndex,
  parseMessages,
  setDefault,
  validateRequestAccountNoCreator,
  validateSubAccount,
} from '../utils/lotte';
import { Errors, Utils } from 'tradex-common';
import { Constants } from '../constants/Constants';
import { IStockBalanceRequest } from '../models/request/IStockBalanceRequest';
import config from '../config';
import {
  ILotteStockBalanceResponse,
  ILotteStockBalanceResponseData,
  ILotteStockBalanceResponseDataListItem,
} from '../models/response/lotte/ILotteStockBalanceResponse';
import { IStockBalanceResponse } from '../models/response/IStockBalanceResponse';
import { IBankListRequest } from '../models/request/IBankAccountRequest';
import { IWithdrawHistoryRequest } from '../models/request/IWithdrawHistoryRequest';
import { IWithdrawHistoryResponse } from '../models/response/IWithdrawHistoryResponse';
import {
  ILotteWithdrawHistoryResponse,
  ILotteWithdrawHistoryResponseData,
} from '../models/response/lotte/ILotteWithdrawHistoryResponse';
import { IWithdrawRequestRequest } from '../models/request/IWithdrawRequestRequest';
import { ILotteWithdrawRequestRequest } from '../models/request/lotte/ILotteWithdrawRequestRequest';
import { ILotteWithdrawRequestResponse } from '../models/response/lotte/ILotteWithdrawRequestResponse';
import { IBankListResponse, IWithdrawBankListResponse } from '../models/response/IBankListResponse';
import { ILotteBankListResponse, ILotteBankListResponseData } from '../models/response/lotte/ILotteBankListResponse';
import { ILotteProfitLossHistoryRequest } from '../models/request/lotte/ILotteProfitLossHistoryRequest';
import { ILotteStockBalanceRequest } from '../models/request/lotte/ILotteStockBalanceRequest';
import { ILotteBankListRequest } from '../models/request/lotte/ILotteBankListRequest';
import { ILotteWithdrawHistoryRequest } from '../models/request/lotte/ILotteWithdrawHistoryRequest';
import { getKeyByValue, LOTTE_LANG_CODE, SUB_TYPE, WITHDRAW_STATUS } from '../constants/enum';
import { ICancelWithdrawRequest } from '../models/request/ICancelWithdrawRequest';
import { ILotteCancelWithdrawRequest } from '../models/request/lotte/ILotteCancelWithdrawRequest';
import { ILotterCancelWithdrawResponse } from '../models/response/lotte/ILotteCancelWithdrawResponse';
import { AccountBankInfo } from '../models/db/AccountBankInfo';
import { ISubListRequest } from '../models/request/ISubListRequest';
import { ISubListResponse } from '../models/response/ISubListResponse';
import { ILotteSubListRequest } from '../models/request/lotte/ILotteSubListRequest';
import { ILotteSubListResponse, ILotteSubListResponseData } from '../models/response/lotte/ILotteSubListResponse';
import { InjectRepository } from 'typeorm-typedi-extensions';
import { AccountBankInfoRepository } from '../repositories/AccountBankInfoRepository';
import { IQueryNavHistoryRequest } from '../models/request/IQueryNavHistoryRequest';
import { IQueryNavHistoryResponse } from '../models/response/IQueryNavHistoryResponse';
import { ILotteQueryNavHistoryRequest } from '../models/request/lotte/ILotteQueryNavHistoryRequest';
import {
  ILotteQueryNavHistoryResponse,
  ILotteQueryNavHistoryResponseData,
} from '../models/response/lotte/ILotteQueryNavHistoryResponse';

const { GeneralError, InvalidParameterError } = Errors;
const { validate, formatDateToDisplay, DATE_DISPLAY_FORMAT } = Utils;

@Service()
export class BalanceService {
  @Inject()
  private lotteBalanceDao: LotteBalanceDao;
  @InjectRepository()
  private accountBankInfoRepository: AccountBankInfoRepository;

  async getProfitLossHistory(request: IProfitLossHistoryRequest, ctx: IContext): Promise<IProfitLossHistoryResponse> {
    const error = new InvalidParameterError();
    validate(request.fromDate, 'fromDate')
      .setRequire()
      .throwValid(error);
    error.throwErr();
    const lotteRequest: ILotteProfitLossHistoryRequest = {
      acnt_no: setDefault<string>(request.accountNumber, request.headers.token.userData.username).toUpperCase(),
      sub_no: setDefault<string>(request.subNumber, config.defaultSubNumber),
      stk_cd:
        setDefault<string>(request.stockCode, 'ALL').toUpperCase() === 'ALL'.toUpperCase()
          ? config.defaultStockCode
          : request.stockCode,
      st_dt: request.fromDate,
      ed_dt: setDefault<string>(request.toDate, formatDateToDisplay(new Date(), DATE_DISPLAY_FORMAT)),
      next_data: setDefault<string>(request.nextKey, config.defaultNextKeyAscOrder),
      row_count: setDefault<number>(request.fetchCount, config.defaultFetchCount).toString(),
    };
    const lotteRes: ILotteProfitLossHistoryResponse = await this.lotteBalanceDao.getProfitLossHistory(
      lotteRequest,
      ctx
    );
    const { codes, messages } = parseMessages(lotteRes.error_desc, lotteRes.error_code);
    if (codes === null || codes === '0011') {
      const lotteResDataList: ILotteProfitLossHistoryResponseData = getElementAtIndex(lotteRes.data_list);
      let items: IProfitLossHistoryResponseItem[] = [];
      if (lotteResDataList.listItems != null) {
        items = lotteResDataList.listItems.map((item: ILotteProfitLossHistoryResponseDataItem) => {
          const response: IProfitLossHistoryResponseItem = {
            accountNumber: item.acnt_no,
            subNumber: item.sub_no,
            tradingDate: item.mth_dt,
            stockCode: item.stk_cd,
            nextKey: lotteResDataList.next_data,
            saleVolume: Number(item.sb_qty),
            capitalCost: Number(item.cost_pri),
            sellPrice: Number(item.sb_pri),
            feeAndTax: Number(item.tot_fee_tax),
            sellAmount: Number(item.adj_amt),
            costValue: Number(item.cost_amt),
            profitLoss: Number(item.pl_amt),
            profitLossRatio: Number(item.pl_rt),
          };
          return response;
        });
      }
      const response: IProfitLossHistoryResponse = {
        totalSellAmount: Number(lotteResDataList.tot_adj_amt),
        totalCostValue: Number(lotteResDataList.tot_cost_amt),
        totalProfitLoss: Number(lotteResDataList.tot_pl_amt),
        totalProfitLossRatio: Number(lotteResDataList.tot_pl_rt),
        items,
      };
      return response;
    } else if (codes !== null || codes === '2016') {
      return {};
    } else {
      throw new GeneralError(`${Constants.ACCOUNT_PROFIT_LOSS_HISTORY}${messages}`);
    }
  }

  async getStockBalance(request: IStockBalanceRequest, ctx: IContext): Promise<IStockBalanceResponse[]> {
    const error = new InvalidParameterError();
    validate(request.accountNumber, 'accountNumber')
      .setRequire()
      .add(validateRequestAccountNoCreator(request))
      .throwValid(error);
    error.throwErr();
    const lotteRequest: ILotteStockBalanceRequest = {
      acnt_no: request.accountNumber.toUpperCase(),
      sub_no: setDefault<string>(request.subNumber, config.defaultSubNumber),
      bank_cd: request.bankName,
      next_data: setDefault<string>(request.lastStockCode, '').padStart(20, '0'),
      row_count: setDefault<number>(request.fetchCount, config.defaultFetchCount).toString(),
    };
    const lotteRes: ILotteStockBalanceResponse = await this.lotteBalanceDao.getStockBalance(lotteRequest, ctx);
    const { codes, messages } = parseMessages(lotteRes.error_desc, lotteRes.error_code);
    if (codes === null || codes === '0011') {
      return lotteRes.data_list.reduce((acc: IStockBalanceResponse[], item: ILotteStockBalanceResponseData) => {
        const itemResponses: IStockBalanceResponse[] = [];
        if (item.listItems != null) {
          item.listItems.forEach((listItem: ILotteStockBalanceResponseDataListItem) => {
            const itemResponse: IStockBalanceResponse = {
              stockCode: listItem.stk_cd,
              balanceQuantity: Number(listItem.own_qty),
              buyAmount: Number(listItem.book_amt),
              evaluationAmount: Number(listItem.eval_amt),
              pendingSellQuantity: Number(listItem.td_sell_mth_qty),
              pendingBuyQuantity: Number(listItem.td_buy_mth_qty),
            };
            itemResponses.push(itemResponse);
          });
        }
        return acc.concat(itemResponses);
      }, []);
    } else if (codes !== null || codes === '2016') {
      return [];
    } else {
      throw new GeneralError(`${Constants.ACCOUNT_STOCK_BALANCE}${messages}`);
    }
  }

  async getAccountBanks(request: IBankListRequest, ctx: IContext): Promise<IBankListResponse[]> {
    const error = new InvalidParameterError();
    validate(request.accountNumber, 'accountNumber')
      .setRequire()
      .add(validateRequestAccountNoCreator(request))
      .throwValid(error);
    validate(request.subNumber, 'subNumber')
      .setRequire()
      .throwValid(error);
    error.throwErr();
    const bankInfos: AccountBankInfo[] = await this.accountBankInfoRepository.find({
      username: request.accountNumber.toUpperCase(),
      subNumber: request.subNumber,
    });
    return bankInfos.map(
      (item: AccountBankInfo): IBankListResponse => ({
        bankCode: item.bankCode,
        bankName: item.bankName,
        bankAccount: item.bankAccount,
      })
    );
  }

  async getWithdrawBanks(request: IBankListRequest, ctx: IContext): Promise<IWithdrawBankListResponse[]> {
    const error = new InvalidParameterError();
    validate(request.accountNumber, 'accountNumber')
      .setRequire()
      .add(validateRequestAccountNoCreator(request))
      .throwValid(error);
    error.throwErr();
    const language = request.headers['accept-language'];
    const lotteResquest: ILotteBankListRequest = {
      lang_code:
        language == null || LOTTE_LANG_CODE[language] == null ? config.defaultLanguage : LOTTE_LANG_CODE[language],
      acnt_no: request.accountNumber.toUpperCase(),
    };
    const lotteRes: ILotteBankListResponse = await this.lotteBalanceDao.getBankAccount(lotteResquest, ctx);
    const bankList: IWithdrawBankListResponse[] = lotteRes.data_list.map(
      (item: ILotteBankListResponseData): IWithdrawBankListResponse => ({
        bankCode: item.bank_code,
        bankName: item.bank_name,
        bankAccountNumber: item.bank_account,
        branchCode: item.bank_branch,
        bankAccountName: item.bank_accountname,
      })
    );
    return bankList;
  }

  async getWithdrawHistory(request: IWithdrawHistoryRequest, ctx: IContext): Promise<IWithdrawHistoryResponse[]> {
    const error = new InvalidParameterError();
    validate(request.accountNumber, 'accountNumber')
      .setRequire()
      .add(validateRequestAccountNoCreator(request))
      .throwValid(error);
    validate(request.status, 'status')
      .setRequire()
      .throwValid(error);
    error.throwErr();
    const lotteRequest: ILotteWithdrawHistoryRequest = {
      acnt_no: request.accountNumber.toUpperCase(),
      sub_no: setDefault<string>(request.subNumber, config.defaultSubNumber),
      from_date: setDefault<string>(request.fromDate, formatDateToDisplay(new Date(), DATE_DISPLAY_FORMAT)),
      to_date: setDefault<string>(request.toDate, formatDateToDisplay(new Date(), DATE_DISPLAY_FORMAT)),
      next_key: setDefault<string>(request.next, config.defaultNextKeyDesc),
      row_count: setDefault<number>(request.fetchCount, config.defaultFetchCount).toString(),
      proc_tp: setDefault<string>(WITHDRAW_STATUS[request.status], request.status),
    };
    const lotteRes: ILotteWithdrawHistoryResponse = await this.lotteBalanceDao.getWithdrawHistory(lotteRequest, ctx);
    const { codes, messages } = parseMessages(lotteRes.error_desc, lotteRes.error_code);
    if (codes === null || codes === '0011') {
      return lotteRes.data_list.map(
        (item: ILotteWithdrawHistoryResponseData): IWithdrawHistoryResponse => ({
          transactionDate: item.date,
          transactionSequenceNumber: Number(item.tr_seq),
          sequenceNumber: Number(item.seq),
          amount: Number(item.amount),
          bank: item.bank,
          bankAccount: item.bank_account,
          note: item.remark,
          isCancel: item.cancel_yn === 'Y',
          approver: item.approved_by,
          approvalDate: item.approved_at,
          next: item.nextkey,
          bankCode: item.bank.substring(0, item.bank.indexOf('.')),
          bankName: item.bank.substring(item.bank.indexOf('.') + 1),
        })
      );
    } else if (codes !== null || codes === '2016') {
      return [];
    } else {
      throw new GeneralError(`${Constants.WITHDRAW_HISTORY}${messages}`);
    }
  }

  async requestWithdraw(request: IWithdrawRequestRequest, ctx: IContext): Promise<IParam> {
    const grantType: string = request.headers?.token?.grantType;
    const error = new InvalidParameterError();
    validate(request.accountNumber, 'accountNumber')
      .setRequire()
      .add(validateRequestAccountNoCreator(request))
      .throwValid(error);
    validate(request.bankAccount, 'bankAccount')
      .setRequire()
      .throwValid(error);
    validate(request.amount, 'amount')
      .setRequire()
      .throwValid(error);
    validate(request.note, 'note')
      .setRequire()
      .throwValid(error);
    validate(request.subNumber, 'subNumber')
      .add(validateSubAccount(request, false))
      .throwValid(error);
    if (grantType !== 'client_credentials') {
      validate(request.bankCode, 'bankCode')
        .setRequire()
        .throwValid(error);
    }
    error.throwErr();
    const language: string = request.headers['accept-language'];
    const subNumber: string = setDefault<string>(request.subNumber, config.defaultSubNumber);
    let bankCode: string = request.bankCode;
    if (Utils.isEmpty(bankCode) && grantType === 'client_credentials') {
      const bankInfo: { [key: string]: string[] } = request.headers.token.userData['bankInfo'];
      bankCode = await getBankCode(request.accountNumber, subNumber, bankInfo, this.accountBankInfoRepository);
    }
    const lotteRequest: ILotteWithdrawRequestRequest = {
      lang_code:
        language == null || LOTTE_LANG_CODE[language] == null ? config.defaultLanguage : LOTTE_LANG_CODE[language],
      acnt_no: request.accountNumber.toUpperCase(),
      sub_no: subNumber,
      amount: request.amount,
      bank_account: request.bankAccount,
      bank_code: bankCode,
      remark: request.note,
      hts_user_id: request.headers.token.userData.username,
    };
    const lotteRes: ILotteWithdrawRequestResponse = await this.lotteBalanceDao.requestWithdraw(lotteRequest, ctx);
    const { codes, messages } = parseMessages(lotteRes.error_desc, lotteRes.error_code);
    if (codes === null || codes === '0012' || codes === '0010') {
      return {
        message: messages,
      };
    } else {
      throw new GeneralError(`${Constants.WITHDRAW_REQUEST}${messages}`);
    }
  }

  async cancelWithdraw(request: ICancelWithdrawRequest, ctx: IContext): Promise<IParam> {
    const grantType: string = request.headers?.token?.grantType;
    const error = new InvalidParameterError();
    validate(request.accountNumber, 'accountNumber')
      .setRequire()
      .add(validateRequestAccountNoCreator(request))
      .throwValid(error);
    validate(request.sequenceNumber, 'sequenceNumber')
      .setRequire()
      .throwValid(error);
    validate(request.bankAccount, 'bankAccount')
      .setRequire()
      .throwValid(error);
    validate(request.amount, 'amount')
      .setRequire()
      .throwValid(error);
    validate(request.subNumber, 'subNumber')
      .add(validateSubAccount(request, false))
      .throwValid(error);
    if (grantType !== 'client_credentials') {
      validate(request.bankCode, 'bankCode')
        .setRequire()
        .throwValid(error);
    }
    error.throwErr();
    const subNumber: string = setDefault<string>(request.subNumber, config.defaultSubNumber);
    let bankCode: string = request.bankCode;
    if (Utils.isEmpty(bankCode) && grantType === 'client_credentials') {
      const bankInfo: { [key: string]: string[] } = request.headers.token.userData['bankInfo'];
      bankCode = await getBankCode(request.accountNumber, subNumber, bankInfo, this.accountBankInfoRepository);
    }
    const lotteRequest: ILotteCancelWithdrawRequest = {
      acnt_no: request.accountNumber.toUpperCase(),
      sub_no: subNumber,
      trd_amt: `${request.amount}`,
      seq: request.sequenceNumber,
      bank_cd: bankCode,
    };

    const lotteRes: ILotterCancelWithdrawResponse = await this.lotteBalanceDao.cancelWithdraw(lotteRequest, ctx);
    const { codes, messages } = parseMessages(lotteRes.error_desc, lotteRes.error_code);
    if (codes === null || codes === '0010') {
      return {
        message: messages,
      };
    } else {
      throw new GeneralError(`${Constants.WITHDRAW_CANCEL}${messages}`);
    }
  }

  async getSubList(request: ISubListRequest, ctx: IContext): Promise<ISubListResponse[]> {
    const error = new InvalidParameterError();
    validate(request.accountNumber, 'accountNumber')
      .setRequire()
      .add(validateRequestAccountNoCreator(request))
      .throwValid(error);
    error.throwErr();
    const lotteRequest: ILotteSubListRequest = {
      acnt_no: request.accountNumber.toUpperCase(),
    };
    const lotteRes: ILotteSubListResponse = await this.lotteBalanceDao.getSubList(lotteRequest, ctx);
    const { codes } = parseMessages(lotteRes.error_desc, lotteRes.error_code);
    if (codes === null || codes === '0010') {
      return lotteRes.data_list.map(
        (item: ILotteSubListResponseData): ISubListResponse => ({
          subNumber: item.sub_no,
          subType: getKeyByValue(item.sub_tp, SUB_TYPE),
        })
      );
    } else {
      throw new GeneralError(lotteRes.error_desc);
    }
  }

  async queryNavHistory(request: IQueryNavHistoryRequest, ctx: IContext): Promise<IQueryNavHistoryResponse[]> {
    const error = new InvalidParameterError();
    validate(request.accountNumber, 'accountNumber')
      .setRequire()
      .add(validateRequestAccountNoCreator(request))
      .throwValid(error);
    validate(request.subNumber, 'subNumber')
      .setRequire()
      .throwValid(error);
    validate(request.toDate, 'toDate')
      .setRequire()
      .throwValid(error);
    error.throwErr();
    const lotteRequest: ILotteQueryNavHistoryRequest = {
      acnt_no: request.accountNumber.toUpperCase(),
      sub_no: request.subNumber,
      begin_dt: setDefault<string>(request.fromDate, formatDateToDisplay(new Date(), DATE_DISPLAY_FORMAT)),
      end_dt: request.toDate,
      next_data: setDefault<string>(request.nextKey, '%'),
      row_count: setDefault<number>(request.fetchCount, config.defaultFetchCount).toString(),
    };
    const lotteRes: ILotteQueryNavHistoryResponse = await this.lotteBalanceDao.queryNavHistory(lotteRequest, ctx);
    const { codes, messages } = parseMessages(lotteRes.error_desc, lotteRes.error_code);
    if (codes === null || codes === '0011') {
      return lotteRes.data_list.map(
        (item: ILotteQueryNavHistoryResponseData): IQueryNavHistoryResponse => ({
          date: item.date,
          netAsset: Number(item.net_asset_val),
          cashIn: Number(item.cash_in),
          cashOut: Number(item.cash_out),
          navProfit: Number(item.profit_daily),
          totalProfit: Number(item.profit_total),
          nextKey: item.next_data,
        })
      );
    } else if (codes !== null && codes === '2016') {
      return [];
    } else {
      throw new GeneralError(messages);
    }
  }
}
