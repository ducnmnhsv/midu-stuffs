import { Inject, Service } from 'typedi';
import { IContext } from '../models/IContext';
import { IAccountInfoRequest } from '../models/request/IAccountInfoRequest';
import { IAssetInfoRequest } from '../models/request/IAssetInfoRequest';
import { Errors, Logger, Utils } from 'tradex-common';
import { LotteAccountDao } from '../daos/LotteAccountDao';
import { IAccountInfoResponse } from '../models/response/IAccountInfoResponse';
import { IAssetInfoResponse, toAssetInfoResponse } from '../models/response/IAssetInfoResponse';
import { IBuyableRequest } from '../models/request/IBuyableRequest';
import { ICashBalanceRequest } from '../models/request/ICashBalanceRequest';
import config from '../config';
import { ILotteAccountInfoData, ILotteAccountInfoResponse } from '../models/response/lotte/ILotteAccountInfoResponse';
import { ILotteBuyableData, ILotteBuyableResponse } from '../models/response/lotte/ILotteBuyableResponse';
import { IBuyableResponse } from '../models/response/IBuyableResponse';
import { ICashBalanceResponse } from '../models/response/ICashBalanceResponse';
import { ILotteCashBalanceData, ILotteCashBalanceResponse } from '../models/response/lotte/ILotteCashBalanceResponse';
import { ISellableRequest } from '../models/response/ISellableRequest';
import { ISellableResponse } from '../models/response/ISellableResponse';
import { ILotteSellableResponse, ILotteSellableResponseData } from '../models/response/lotte/ILotteSellableResponse';
import { ILotteAssetInfoData, ILotteAssetInfoResponse } from '../models/response/lotte/ILotteAssetInfoResponse';
import {
  getBankCode,
  getElementAtIndex,
  parseMessages,
  setDefault,
  validateRequestAccountNoCreator,
} from '../utils/lotte';
import { GeneralError } from 'tradex-common/build/src/modules/errors';
import { Constants } from '../constants/Constants';
import { ILotteAccountInfoRequest } from '../models/request/lotte/ILotteAccountInfoRequest';
import { ILotteAssetInfoRequest } from '../models/request/lotte/ILotteAssetInfoRequest';
import { ILotteBuyableRequest } from '../models/request/lotte/ILotteBuyableRequest';
import { ILotteCashBalanceRequest } from '../models/request/lotte/ILotteCashBalanceRequest';
import { ILotteSellableRequest } from '../models/request/lotte/ILotteSellableRequest';
import { IAccountLoanHistoryResponse } from '../models/response/IAccountLoanHistoryResponse';
import { ILotteAccountLoanHistoryResponse } from '../models/response/lotte/ILotteAccountLoanHistoryResponse';
import { ILotteAccountLoanHistoryRequest } from '../models/request/lotte/ILotteAccountLoanHistoryRequest';
import { IAccountLoanHistoryRequest } from '../models/request/IAccountLoanHistoryRequest';

import { IAccountProfitLossRequest } from '../models/request/IAccountProfitLossRequest';
import { IAccountProfitLossResponse, IProfitLossItem } from '../models/response/IAccountProfitLossResponse';
import { ILotteAccountBalanceRetrieveRequest } from '../models/request/lotte/ILotteAccountBalanceRetrieveRequest';
import { ILotteAccountStkBalanceRequest } from '../models/request/lotte/ILotteAccountStkBalanceRequest';
import { LotteBalanceDao } from '../daos/LotteBalanceDao';
import { ILotteAccountBalanceRetrieveResponseData } from '../models/response/lotte/ILotteAccountBalanceRetrieveResponse';
import {
  ILotteAccountStkBalanceResponseData,
  ILotteAccountStkBalanceResponseDataItem,
} from '../models/response/lotte/ILotteAccountStkBalanceResponse';
import { IMarginAccountRequest } from '../models/request/IMarginAccountRequest';
import { ILotteMarginAccountRequest } from '../models/request/lotte/ILotteMarginAccountRequest';
import {
  ILotteMarginAccountResponse,
  ILotteMarginAccountResponseData,
} from '../models/response/lotte/ILotteMarginAccountResponse';
import { IMarginAccountResponse } from '../models/response/IMarginAccountResponse';
import { IVsdStatusAccountRequest } from '../models/request/IVsdStatusAccountRequest';
import { IVsdStatusAccountResponse } from '../models/response/IVsdStatusAccountResponse';
import { ILotteVsdStatusAccountRequest } from '../models/request/lotte/ILotteVsdStatusAccountRequest';
import { ILotteVsdStatusAccountResponse } from '../models/response/lotte/ILotteVsdStatusAccountResponse';
import { VSD_STATUS } from '../constants/enum';
import { ILotteAccountContractStatusRequest } from '../models/request/lotte/ILotteAccountContractStatusRequest';
import {
  ILotteNotificationStatusResponse,
  ILotteNotificationStatusResponseData,
} from '../models/response/lotte/ILotteNotificationStatusResponse';
import { IAccountContractStatusResponse } from '../models/response/IAccountContractStatusResponse';
import { IAccountContractStatusRequest } from '../models/request/IAccountContractStatusRequest';
import { INotificationStatusRequest } from '../models/request/INotificationStatusRequest';
import { ILotteContractStatusResponse } from '../models/response/lotte/ILotteContractStatusResponse';
import { ILotteNotificationStatusRequest } from '../models/request/lotte/ILotteNotificationStatusRequest';
import { INotificationStatusResponse } from '../models/response/INotificationStatusResponse';
import { IEstAssetLoanInfoRequest } from '../models/request/IEstAssetLoanInfoRequest';
import { IEstAssetLoanInfoResponse } from '../models/response/IEstAssetLoanInfoResponse';
import { ILotteEstAssetLoanInfoRequest } from '../models/request/lotte/ILotteEstAssetLoanInfoRequest';
import {
  ILotteEstAssetLoanInfoResponse,
  ILotteEstAssetLoanInfoResponseData,
} from '../models/response/lotte/ILotteEstAssetLoanInfoResponse';
import { InjectRepository } from 'typeorm-typedi-extensions';
import { AccountBankInfoRepository } from '../repositories/AccountBankInfoRepository';
import { ICashDepositHistoryRequest } from '../models/request/ICashDepositHistoryRequest';
import { ICashDepositHistoryItemResponse } from '../models/response/ICashDepositHistoryResponse';
import { ILotteCashDepositHistoryRequest } from '../models/request/lotte/ILotteCashDepositHistoryRequest';
import { ILotteCashDepositHistoryResponse, ILotteCashDepositHistoryResponseData } from '../models/response/lotte/ILotteCashDepositHistoryResponse';
import { LOTTE_LANG_CODE, CASH_DEPOSIT_HISTORY_TYPE, getChangeBrokerReasonCodes, getChangeBrokerReasonLabel, AccountType } from '../constants/enum';
import { isEmpty } from 'tradex-common/build/src/modules/utils/StringUtils';
import { IChangeBrokerRequest } from '../models/request/IChangeBrokerRequest';
import { IChangeBrokerHistoryRequest } from '../models/request/IChangeBrokerHistoryRequest';
import { IChangeBrokerResponse } from '../models/response/IChangeBrokerResponse';
import { IChangeBrokerHistoryItemResponse } from '../models/response/IChangeBrokerHistoryResponse';
import { ILotteChangeBrokerRequest } from '../models/request/lotte/ILotteChangeBrokerRequest';
import { ILotteChangeBrokerResponse, ILotteChangeBrokerData } from '../models/response/lotte/ILotteChangeBrokerResponse';
import { AccountChangeBrokerRequestRepository } from '../repositories/AccountChangeBrokerRequestRepository';
import { AccountChangeBrokerRequest, ChangeBrokerStatus } from '../models/db/AccountChangeBrokerRequest';
import { calculateExpiredDate, formatDateVietnam, sendEmailNotification, sendSmsNotification } from '../utils/brokerUtils';
import { ILotteEmployeeInfoRequest } from '../models/request/lotte/ILotteEmployeeInfoRequest';
import { IRegisterBankAccountRequest, IDeleteBankAccountRequest } from '../models/request/IBankAccountRequest';
import {
  ILotteRegisterBankAccountRequest,
  ILotteDeleteBankAccountRequest,
} from '../models/request/lotte/ILotteBankAccountRequest';
import { IRegisterBankAccountResponse, IDeleteBankAccountResponse } from '../models/response/IBankAccountResponse';

const { InvalidParameterError } = Errors;
const { validate, formatDateToDisplay, convertStringToDate, DATE_DISPLAY_FORMAT } = Utils;
const DATE_DISPLAY_FORMAT_DMY = 'DDMMYYYY';
const DATE_DISPLAY_FORMAT_YMD = 'YYYYMMDD';

@Service()
export class AccountService {
  @Inject()
  private lotteAccountDao: LotteAccountDao;
  @Inject()
  private lotteBalanceDao: LotteBalanceDao;
  @InjectRepository()
  private accountBankInfoRepository: AccountBankInfoRepository;
  @InjectRepository()
  private accountChangeBrokerRequestRepository: AccountChangeBrokerRequestRepository;

  async getAccountInfo(request: IAccountInfoRequest, ctx: IContext): Promise<IAccountInfoResponse> {
    const accountNumber = setDefault<string>(request.accountNumber, request.headers.token.userData.username);
    if (accountNumber.toLowerCase() !== request.headers.token.userData.username.toLowerCase()) {
      throw new InvalidParameterError().add('INVALID_ACCOUNT_NUMBER', 'accountNumber', [request.accountNumber]);
    }
    const lotteRequest: ILotteAccountInfoRequest = {
      acnt_no: accountNumber.toUpperCase(),
    };
    const lotteRes: ILotteAccountInfoResponse = await this.lotteAccountDao.getAccountInfo(lotteRequest, ctx);
    const { codes } = parseMessages(lotteRes.error_desc, lotteRes.error_code);
    const lotteResDataList: ILotteAccountInfoData = getElementAtIndex<ILotteAccountInfoData>(lotteRes.data_list);
    if (codes === null || codes === '0011') {
      const response: IAccountInfoResponse = {
        username: accountNumber.toUpperCase(),
        email: lotteResDataList.email,
        address: lotteResDataList.address,
        phoneNumber: lotteResDataList.phone,
        identifierNumber: lotteResDataList.identity_card,
        customerName: lotteResDataList.customer_name,
        agencyName: lotteResDataList.manager,
        agencyId: lotteResDataList.emp_no,
        accountType: this.mapAccountType(lotteResDataList.grp_tp),
        dateOfBirth: this.formatLotteDateDMY(lotteResDataList.birth_dt),
        identifierIssueDate: this.formatLotteDateDMY(lotteResDataList.idno_iss_dt),
        identifierExpireDate: this.formatLotteDateYMD(lotteResDataList.idno_expr_dt),
        issuePlace: lotteResDataList.idno_iss_orga,
        isForeignCustomer: this.mapForeignCustomer(lotteResDataList.frgn_tp),
        taxCode: lotteResDataList.tax_cd,
      };
      return response;
    } else if (codes !== null && codes === '2016') {
      return {};
    } else {
      throw new GeneralError(`${Constants.ACCOUNT_INFO}${codes}`);
    }
  }

  private mapAccountType(grpTp: string): AccountType | undefined {
    if (grpTp === '1') {
      return AccountType.INDIVIDUAL;
    } else if (grpTp === '2') {
      return AccountType.INSTITUTION;
    }
    return undefined;
  }

  private mapForeignCustomer(frgnTp: string): boolean | null {
    if (frgnTp === '1') {
      return false;
    } else if (frgnTp === '2') {
      return true;
    }
    return null;
  }

  private formatLotteDateDMY(dateStr: string): string | undefined {
    if (!dateStr || dateStr.trim() === '') {
      return undefined;
    }
    if (dateStr.length === 8) {
      return formatDateToDisplay(convertStringToDate(dateStr, DATE_DISPLAY_FORMAT_DMY), DATE_DISPLAY_FORMAT);
    }
    return dateStr;
  }

  private formatLotteDateYMD(dateStr: string): string | undefined {
    if (!dateStr || dateStr.trim() === '') {
      return undefined;
    }
    if (dateStr.length === 8) {
      return formatDateToDisplay(convertStringToDate(dateStr, DATE_DISPLAY_FORMAT_YMD), DATE_DISPLAY_FORMAT);
    }
    return dateStr;
  }

  // private getAccountInfoFromCache = async (accountNumber: string, ctx: IContext): Promise<IAccountInfoResponse> => {
  //   try {
  //     return await this.redis.get<IAccountInfoResponse>(Category.USER_ACC_INFO, accountNumber);
  //   } catch (e) {
  //     Logger.error(ctx.id, 'Error when get account info from cache:', e);
  //     return null;
  //   }
  // };

  async getAssetInfo(request: IAssetInfoRequest, ctx: IContext): Promise<IAssetInfoResponse> {
    const error = new InvalidParameterError();
    validate(request.accountNumber, 'accountNumber')
      .setRequire()
      .add(validateRequestAccountNoCreator(request))
      .throwValid(error);
    error.throwErr();
    const subNumber = setDefault<string>(request.subNumber, config.defaultSubNumber);
    const bankInfo = request.headers.token.userData['bankInfo'];
    const bankCode: string = await getBankCode(
      request.accountNumber,
      subNumber,
      bankInfo,
      this.accountBankInfoRepository
    );
    const lotteRequest: ILotteAssetInfoRequest = {
      acnt_no: request.accountNumber.toUpperCase(),
      sub_no: setDefault<string>(request.subNumber, config.defaultSubNumber),
      bank_code: setDefault<string>(request.bankCode, setDefault<string>(bankCode, config.defaultBankCode)),
    };
    const lotteRes: ILotteAssetInfoResponse = await this.lotteAccountDao.getAssetInfo(lotteRequest, ctx);
    const { codes } = parseMessages(lotteRes.error_desc, lotteRes.error_code);
    const lotteResDataList: ILotteAssetInfoData = getElementAtIndex<ILotteAssetInfoData>(lotteRes.data_list);
    if (codes === null || codes === '0011') {
      const response: IAssetInfoResponse = toAssetInfoResponse(lotteResDataList);
      return response;
    } else if (codes !== null && codes === '2016') {
      return {};
    } else {
      throw new GeneralError(`${Constants.ACCOUNT_ASSET_INFO}${codes}`);
    }
  }

  async getBuyable(request: IBuyableRequest, ctx: IContext): Promise<IBuyableResponse> {
    const error = new InvalidParameterError();
    validate(request.accountNumber, 'accountNumber')
      .setRequire()
      .add(validateRequestAccountNoCreator(request))
      .throwValid(error);
    validate(request.stockCode, 'stockCode')
      .setRequire()
      .throwValid(error);
    validate(request.orderPrice, 'orderPrice')
      .setRequire()
      .throwValid(error);
    error.throwErr();
    const subNumber = setDefault<string>(request.subNumber, config.defaultSubNumber);
    const bankInfo = request.headers.token.userData['bankInfo'];
    const bankCode: string = await getBankCode(
      request.accountNumber,
      subNumber,
      bankInfo,
      this.accountBankInfoRepository
    );
    const lotteRequest: ILotteBuyableRequest = {
      acnt_no: request.accountNumber.toUpperCase(),
      sub_no: subNumber,
      bank_cd: setDefault<string>(request.bankCode, setDefault<string>(bankCode, config.defaultBankCode)),
      stk_cd: setDefault<string>(request.stockCode, ''),
      mkt_trd_tp: setDefault<string>(request.marketType, 'HOSE'),
      ord_qty: setDefault<string>(request.orderPrice, '0'),
      ord_pri: request.orderPrice,
    };
    const lotteRes: ILotteBuyableResponse = await this.lotteAccountDao.getBuyable(lotteRequest, ctx);
    const { codes } = parseMessages(lotteRes.error_desc, lotteRes.error_code);
    const lotteResDataList: ILotteBuyableData = getElementAtIndex<ILotteBuyableData>(lotteRes.data_list);
    if (codes === null || codes === '0011') {
      const response: IBuyableResponse = {
        lackAmount: Number(lotteResDataList.lack_blk_amt),
        buyingPower: Number(lotteResDataList.buying_power),
        depositAmount: Number(lotteResDataList.dpo),
        buyableQuantity: Number(lotteResDataList.buy_abl_qty),
        marginLimitation: Number(lotteResDataList.max_loan_amt),
        orderBlockAmount: Number(lotteResDataList.td_total_porf),
        totalBlockAmount: Number(lotteResDataList.bfr_total_block),
        assetValuationAmount: Number(lotteResDataList.init_asst),
        stockValuationAmount: Number(lotteResDataList.buy_abl_amt),
        virtualDepositAmount: Number(lotteResDataList.gst_dpo),
      };
      return response;
    } else if (codes !== null && codes === '2016') {
      return {};
    } else {
      throw new GeneralError(`${Constants.ACCOUNT_BUY_ABLE}${codes}`);
    }
  }

  async getCashBalance(request: ICashBalanceRequest, ctx: IContext): Promise<ICashBalanceResponse> {
    const error = new InvalidParameterError();
    validate(request.accountNumber, 'accountNumber')
      .setRequire()
      .add(validateRequestAccountNoCreator(request))
      .throwValid(error);
    error.throwErr();
    const lotteRequest: ILotteCashBalanceRequest = {
      acnt_no: request.accountNumber.toUpperCase(),
      sub_no: setDefault<string>(request.subNumber, config.defaultSubNumber),
    };
    const lotteRes: ILotteCashBalanceResponse = await this.lotteAccountDao.getCashBalance(lotteRequest, ctx);
    const { codes } = parseMessages(lotteRes.error_desc, lotteRes.error_code);
    const lotteResDataList: ILotteCashBalanceData = getElementAtIndex<ILotteCashBalanceData>(lotteRes.data_list);
    if (codes === null || codes === '0011') {
      const response: ICashBalanceResponse = {
        depositAmount: Number(lotteResDataList.dpo),
        depositBlockAmount: Number(lotteResDataList.dpo_block),
        expiredLoanAmount: Number(lotteResDataList.nonrpy_loan_amt),
        waitSellAmount: Number(lotteResDataList.mth_amt),
        stockEvaluationAmount: Number(lotteResDataList.sbst_dpo),
        marginLoanAmount: Number(lotteResDataList.mgn_lack),
        securedLoanAmount: Number(lotteResDataList.cd_lack),
        orderBlockAmount: Number(lotteResDataList.all_prf),
        withdrawableAmount: Number(lotteResDataList.tot_out_psbamt),
      };
      return response;
    } else if (codes !== null && codes === '2016') {
      return {};
    } else {
      throw new GeneralError(`${Constants.ACCOUNT_CASH_BALANCE}${codes}`);
    }
  }

  async getLoanHistory(request: IAccountLoanHistoryRequest, ctx: IContext): Promise<IAccountLoanHistoryResponse[]> {
    const error = new InvalidParameterError();
    validate(request.accountNumber, 'accountNumber')
      .setRequire()
      .add(validateRequestAccountNoCreator(request))
      .throwValid(error);
    error.throwErr();
    const lotteRequest: ILotteAccountLoanHistoryRequest = {
      acnt_no: request.accountNumber.toUpperCase(),
      sub_no: setDefault<string>(request.subNumber, config.defaultSubNumber),
      next_key: setDefault<string>(request.nextKey, config.defaultNextKeyAscOrder),
      row_count: setDefault<number>(request.fetchCount, config.defaultFetchCount).toString(),
    };
    const lotteRes: ILotteAccountLoanHistoryResponse = await this.lotteAccountDao.getLoanHistory(lotteRequest, ctx);
    const { codes } = parseMessages(lotteRes.error_desc, lotteRes.error_code);
    if (codes === null || codes === '0011') {
      return lotteRes.data_list.map(
        (item): IAccountLoanHistoryResponse => ({
          loanDate: formatDateToDisplay(convertStringToDate(item.lnd_dt, DATE_DISPLAY_FORMAT_DMY), DATE_DISPLAY_FORMAT),
          expiredDate: formatDateToDisplay(
            convertStringToDate(item.expr_dt, DATE_DISPLAY_FORMAT_DMY),
            DATE_DISPLAY_FORMAT
          ),
          stockCode: item.stk_cd,
          loanType: item.lnd_tp,
          loanQuantity: Number(item.lnd_qty),
          loanAmount: Number(item.lnd_amt),
          loanInterest: Number(item.lnd_int_rm),
          loanRepayAmount: Number(item.lnd_rpy_amt),
          loanRemainAmount: Number(item.lnd_rm_amt),
          status: item.expr_dt_tp,
          totalLoan: Number(item.tot_lnd),
          nextKey: item.next,
        })
      );
    } else if (codes !== null && codes === '2016') {
      return [];
    } else {
      throw new GeneralError(`${Constants.ACCOUNT_LOAN_HISTORY}${codes}`);
    }
  }

  async getSellable(request: ISellableRequest, ctx: IContext): Promise<ISellableResponse[]> {
    const error = new InvalidParameterError();
    validate(request.accountNumber, 'accountNumber')
      .setRequire()
      .add(validateRequestAccountNoCreator(request))
      .throwValid(error);
    error.throwErr();
    const lotteRequest: ILotteSellableRequest = {
      acnt_no: request.accountNumber.toUpperCase(),
      sub_no: setDefault<string>(request.subNumber, config.defaultSubNumber),
      next_key: setDefault<string>(request.stockCode, config.defaultNextKeyAscOrder),
      row_count: setDefault<number>(request.fetchCount, config.defaultFetchCount).toString(),
    };
    const lotteRes: ILotteSellableResponse = await this.lotteAccountDao.getSellable(lotteRequest, ctx);
    const { codes } = parseMessages(lotteRes.error_desc, lotteRes.error_code);
    if (codes === null || codes === '0011') {
      return lotteRes.data_list.map(
        (item: ILotteSellableResponseData): ISellableResponse => ({
          stockCode: item.stk_code,
          balanceQuantity: Number(item.own_qty),
          sellableQuantity: Number(item.sell_psb_qty),
          t2Sell: Number(item.ppd_sell_mth_qty),
          t2Buy: Number(item.ppd_buy_mth_qty),
          t1Sell: Number(item.pd_sell_mth_qty),
          t1Buy: Number(item.pd_buy_mth_qty),
          todaySell: Number(item.td_sell_mth_qty),
          todayBuy: Number(item.td_buy_mth_qty),
        })
      );
    } else if (codes !== null && codes === '2016') {
      return [];
    } else {
      throw new GeneralError(`${Constants.ACCOUNT_SELL_ABLE}${codes}`);
    }
  }

  async getAccountProfitLoss(request: IAccountProfitLossRequest, ctx: IContext): Promise<IAccountProfitLossResponse> {
    const error = new InvalidParameterError();
    validate(request.accountNumber, 'accountNumber')
      .setRequire()
      .add(validateRequestAccountNoCreator(request))
      .throwValid(error);
    error.throwErr();
    const subNumber = setDefault<string>(request.subNumber, config.defaultSubNumber);
    const bankInfo = request.headers.token.userData['bankInfo'];
    const bankCode: string = await getBankCode(
      request.accountNumber,
      subNumber,
      bankInfo,
      this.accountBankInfoRepository
    );
    const lotteReqBalanceRetrieve: ILotteAccountBalanceRetrieveRequest = {
      acnt_no: request.accountNumber.toUpperCase(),
      sub_no: subNumber,
      bank_cd: setDefault<string>(request.bankCode, setDefault<string>(bankCode, config.defaultBankCode)),
    };
    const lotteReqStkBalance: ILotteAccountStkBalanceRequest = {
      acnt_no: request.accountNumber.toUpperCase(),
      sub_no: setDefault<string>(request.subNumber, config.defaultSubNumber),
      bank_cd: setDefault<string>(request.bankCode, setDefault<string>(bankCode, config.defaultBankCode)),
      next_data: setDefault<string>(request.lastStockCode, config.defaultNextKeyAscOrder),
      row_count: setDefault<number>(request.fetchCount, config.defaultFetchCount).toString(),
    };
    const [lotteResBalanceRetrieve, lotteResStkBalance] = await Promise.all([
      this.lotteAccountDao.balanceRetrieving(lotteReqBalanceRetrieve, ctx),
      this.lotteBalanceDao.inquriyStockBalance(lotteReqStkBalance, ctx),
    ]);
    const lotteResBalanceRetrieveData: ILotteAccountBalanceRetrieveResponseData = getElementAtIndex<
      ILotteAccountBalanceRetrieveResponseData
    >(lotteResBalanceRetrieve.data_list);
    const lotteResStkBalanceData: ILotteAccountStkBalanceResponseData = getElementAtIndex<
      ILotteAccountStkBalanceResponseData
    >(lotteResStkBalance.data_list);
    const codeMessageBalanceRetrieve = parseMessages(
      lotteResBalanceRetrieve.error_desc,
      lotteResBalanceRetrieve.error_code
    );
    const codeMessageStkBalance = parseMessages(lotteResStkBalance.error_desc, lotteResStkBalance.error_code);
    let response: IAccountProfitLossResponse = {};
    if (codeMessageBalanceRetrieve.codes == null || codeMessageBalanceRetrieve.codes === '0011') {
      response = {
        t1Deposit: Number(lotteResBalanceRetrieveData.d1_tot_dpo),
        t2Deposit: Number(lotteResBalanceRetrieveData.d2_tot_dpo),
        depositAmount: Number(lotteResBalanceRetrieveData.dpo),
        totalBuyAmount: Number(lotteResBalanceRetrieveData.tot_book_amt),
        totalProfitLoss: Number(lotteResBalanceRetrieveData.tot_eval_prf),
        estimatedDeposit: Number(lotteResBalanceRetrieveData.presum_dpo),
        totalProfitLossRate: Number(lotteResBalanceRetrieveData.tot_bnf_rt),
        totalEvaluationAmount: Number(lotteResBalanceRetrieveData.tot_eval_amt),
        profitLossItems: [],
      };
    } else if (codeMessageStkBalance.codes != null && codeMessageStkBalance.codes === '2016') {
      return {};
    } else {
      throw new GeneralError(`${Constants.ACCOUNT_PROFIT_LOSS}${codeMessageBalanceRetrieve.codes}`);
    }

    if (codeMessageStkBalance.codes == null || codeMessageStkBalance.codes === '0011') {
      if (lotteResStkBalanceData.listItems != null) {
        response.profitLossItems = lotteResStkBalanceData.listItems.map(
          (item: ILotteAccountStkBalanceResponseDataItem): IProfitLossItem => ({
            t1Buy: Number(item.pd_buy_mth_qty),
            t2Buy: Number(item.ppd_buy_mth_qty),
            t1Sell: Number(item.pd_sell_mth_qty),
            t2Sell: Number(item.ppd_sell_mth_qty),
            todayBuy: Number(item.td_buy_mth_qty),
            stockCode: item.stk_cd,
            todaySell: Number(item.td_sell_mth_qty),
            buyingPrice: Number(item.buy_uv),
            buyingAmount: Number(item.book_amt),
            currentPrice: Number(item.cur_pri),
            buyingQuantity: Number(item.qty),
            profitLossRate: Number(item.eval_per),
            balanceQuantity: Number(item.own_qty),
            evaluationAmount: Number(item.eval_amt),
            sellableQuantity: Number(item.sell_able_qty),
            securedQuantity: Number(item.mrtg_lnd_qty),
            profitLoss: Number(item.pnl_amount),
          })
        );
      }
    } else if (codeMessageStkBalance.codes != null && codeMessageStkBalance.codes === '2016') {
      response.profitLossItems = [];
    } else {
      throw new GeneralError(`${Constants.ACCOUNT_PROFIT_LOSS}${codeMessageStkBalance.codes}`);
    }
    return response;
  }

  async getMarginAccount(request: IMarginAccountRequest, ctx: IContext): Promise<IMarginAccountResponse> {
    const error = new InvalidParameterError();
    validate(request.accountNumber, 'accountNumber')
      .setRequire()
      .add(validateRequestAccountNoCreator(request))
      .throwValid(error);
    validate(request.subNumber, 'subNumber')
      .setRequire()
      .throwValid(error);
    validate(request.symbolCode, 'symbolCode')
      .setRequire()
      .throwValid(error);
    const lotteRequest: ILotteMarginAccountRequest = {
      acnt_no: request.accountNumber,
      sub_no: request.subNumber,
      stk_code: request.symbolCode,
    };
    const lotteRes: ILotteMarginAccountResponse = await this.lotteAccountDao.marginRatio(lotteRequest, ctx);
    const { codes } = parseMessages(lotteRes.error_desc, lotteRes.error_code);
    if (codes === null || codes === '0011') {
      const lotteResDataList: ILotteMarginAccountResponseData = getElementAtIndex<ILotteMarginAccountResponseData>(
        lotteRes.data_list
      );
      return {
        ratio: Number(lotteResDataList.ssr),
      };
    } else if (codes !== null && codes === '2016') {
      return {};
    } else {
      throw new GeneralError(`${Constants.ACCOUNT_MARGIN_RATIO}${codes}`);
    }
  }

  async getAccountVsdStatus(request: IVsdStatusAccountRequest, ctx: IContext): Promise<IVsdStatusAccountResponse> {
    const error = new InvalidParameterError();
    validate(request.accountNumber, 'accountNumber')
      .setRequire()
      .add(validateRequestAccountNoCreator(request))
      .throwValid(error);
    validate(request.subAccount, 'subAccount')
      .setRequire()
      .throwValid(error);
    error.throwErr();

    const lotteRequest: ILotteVsdStatusAccountRequest = {
      acnt_no: request.accountNumber,
      sub_no: request.subAccount,
    };
    ctx.orgMsg.data.headers['accept-language'] = 'vi';
    const lotteRes: ILotteVsdStatusAccountResponse = await this.lotteAccountDao.vsdStatus(lotteRequest, ctx);
    const { codes, messages } = parseMessages(lotteRes.error_desc, lotteRes.error_code);
    Logger.info(ctx.id, 'codes', codes, 'messages', messages);
    if (lotteRes.error_code === '0000') {
      if (lotteRes.data_list && lotteRes.data_list[0] && lotteRes.data_list[0].val) {
        if (lotteRes.data_list[0].val === '') {
          return {
            status: 'UNKNOWN',
          };
        } else {
          return {
            status: VSD_STATUS[lotteRes.data_list[0].val],
          };
        }
      } else {
        throw new GeneralError(`${Constants.ACCOUNT_VSD_STATUS}${codes}`);
      }
    } else {
      throw new GeneralError(`${Constants.ACCOUNT_VSD_STATUS}${codes}`);
    }
  }

  async getAccountContractStatus(
    request: IAccountContractStatusRequest,
    ctx: IContext
  ): Promise<IAccountContractStatusResponse> {
    const error = new InvalidParameterError();
    validate(request.accountNumber, 'accountNumber')
      .setRequire()
      .add(validateRequestAccountNoCreator(request))
      .throwValid(error);
    error.throwErr();
    const lotteAccountContractStatusRequest: ILotteAccountContractStatusRequest = {
      acnt_no: request.accountNumber.toUpperCase(),
    };
    ctx.orgMsg.data.headers['accept-language'] = 'vi';
    const lotteRes: ILotteContractStatusResponse = await this.lotteAccountDao.contractStatus(
      lotteAccountContractStatusRequest,
      ctx
    );
    let codes = null;
    if (lotteRes.error_desc.length > 0) {
      const startIndex = lotteRes.error_desc.indexOf('[');
      const endIndex = lotteRes.error_desc.indexOf(']');
      if (startIndex >= 0 && endIndex > 0) {
        codes = lotteRes.error_desc.substring(startIndex + 2, endIndex);
      }
      if (codes === null) {
        codes = 'INTERNAL_SERVER_ERROR';
      }
    }
    if (lotteRes.error_code === '0000') {
      const hasCntrYn = lotteRes.data_list.length > 0 ? lotteRes.data_list[0].has_cntr_yn : '';
      if (hasCntrYn === 'Y') {
        return {
          contractStatus: 'COMPLETED',
        };
      } else {
        return {
          contractStatus: 'PROCESSING',
        };
      }
    } else if (lotteRes.error_code === '1005' && codes === '2006') {
      return {
        contractStatus: 'COMPLETED',
      };
    } else {
      throw new GeneralError(`${codes}`);
    }
  }

  async updateNotificationStatus(request: INotificationStatusRequest, ctx: IContext): Promise<{}> {
    const iLotteRequest: ILotteNotificationStatusRequest = {
      acnt_no: request.headers.token.userData.accountNumbers[0],
      status: request.notification === 'ON' ? 'Y' : 'N',
    };

    const lotteRes: ILotteNotificationStatusResponse = await this.lotteAccountDao.updateNotificationStatus(
      iLotteRequest,
      ctx
    );

    const { codes } = parseMessages(lotteRes.error_desc, lotteRes.error_code);
    if (lotteRes.error_code === '0000' && lotteRes.success) {
      return {};
    } else {
      throw new GeneralError(`${codes}`);
    }
  }

  async getNotificationStatus(
    request: INotificationStatusRequest,
    ctx: IContext
  ): Promise<INotificationStatusResponse> {
    const iLotteRequest: ILotteNotificationStatusRequest = {
      acnt_no: request.headers.token.userData.accountNumbers[0],
    };

    const lotteRes: ILotteNotificationStatusResponse = await this.lotteAccountDao.getNotificationStatus(
      iLotteRequest,
      ctx
    );
    const { codes } = parseMessages(lotteRes.error_desc, lotteRes.error_code);
    if (lotteRes.error_code === '0000') {
      const lotteResDataList: ILotteNotificationStatusResponseData = getElementAtIndex<
        ILotteNotificationStatusResponseData
      >(lotteRes.data_list);
      return {
        notification: lotteResDataList.status === 'Y' ? 'ON' : 'OFF',
      };
    } else {
      throw new GeneralError(`${codes}`);
    }
  }

  async getEstAssetLoanInfo(request: IEstAssetLoanInfoRequest, ctx: IContext): Promise<IEstAssetLoanInfoResponse> {
    const error = new InvalidParameterError();
    validate(request.accountNumber, 'accountNumber')
      .setRequire()
      .add(validateRequestAccountNoCreator(request))
      .throwValid(error);
    validate(request.subNumber, 'subNumber')
      .setRequire()
      .throwValid(error);
    error.throwErr();
    const lotteRequest: ILotteEstAssetLoanInfoRequest = {
      hts_user_id: request.headers.token.userData.username.toLocaleLowerCase(),
      acnt_no: request.accountNumber.toLocaleUpperCase(),
      sub_no: request.subNumber,
    };
    const lotteRes: ILotteEstAssetLoanInfoResponse = await this.lotteAccountDao.getEstAssetLoanInfo(lotteRequest, ctx);
    const { codes, messages } = parseMessages(lotteRes.error_desc, lotteRes.error_code);
    Logger.info(ctx.id, 'codes', codes, 'messages', messages);
    if (codes === null) {
      const lotteResDataList: ILotteEstAssetLoanInfoResponseData = getElementAtIndex<
        ILotteEstAssetLoanInfoResponseData
      >(lotteRes.data_list);
      return {
        securedAssets: Number(lotteResDataList.sbst_rt),
        realLoan: Number(lotteResDataList.tot_loan_real),
        estimatedCMR: Number(lotteResDataList.cmr),
        estimatedAdditionalAmount: Number(lotteResDataList.short_amt_lmr),
      };
    } else {
      throw new GeneralError(lotteRes.error_desc);
    }
  }

  async getCashDepositHistory(request: ICashDepositHistoryRequest, ctx: IContext): Promise<ICashDepositHistoryItemResponse[]> {
    const error = new InvalidParameterError();
    validate(request.accountNumber, 'accountNumber')
      .setRequire()
      .add(validateRequestAccountNoCreator(request))
      .throwValid(error);
    validate(request.subNumber, 'subNumber')
      .setRequire()
      .throwValid(error);
    error.throwErr();
    const language = request.headers['accept-language'];
    const langCode = language == null || LOTTE_LANG_CODE[language] == null ? config.defaultLanguage : LOTTE_LANG_CODE[language];
    const type = isEmpty(request.type) ? CASH_DEPOSIT_HISTORY_TYPE.ALL : CASH_DEPOSIT_HISTORY_TYPE[request.type];
    const beginDt = isEmpty(request.fromDate) ? formatDateToDisplay(new Date(), DATE_DISPLAY_FORMAT) : request.fromDate;
    const endDt = isEmpty(request.toDate) ? formatDateToDisplay(new Date(), DATE_DISPLAY_FORMAT) : request.toDate;
    const rowCount = request.fetchCount ?? config.defaultFetchCount;
    const lotteReq: ILotteCashDepositHistoryRequest = {
      acnt_no: request.accountNumber.toUpperCase(),
      sub_no: request.subNumber,
      begin_dt: beginDt,
      end_dt: endDt,
      type: type,
      lang_code: langCode,
      next_key: request.nextKey,
      row_count: rowCount.toString(),
    };
    if (!isEmpty(request.nextKey)) {
      lotteReq.next_key = request.nextKey;
    }
    const lotteRes: ILotteCashDepositHistoryResponse = await this.lotteAccountDao.getCashDepositHistory(lotteReq, ctx);
    if (lotteRes.error_code === '0000' && lotteRes.success) {
      const returnResult = lotteRes.data_list.map(
        (item: ILotteCashDepositHistoryResponseData): ICashDepositHistoryItemResponse => ({
          tradeDate: isEmpty(item.trade_dt) ? item.trade_dt : formatDateToDisplay(convertStringToDate(item.trade_dt, DATE_DISPLAY_FORMAT_DMY), DATE_DISPLAY_FORMAT),
          sequence: isEmpty(item.seq_no) ? null : Number(item.seq_no),
          remarkName: item.rmrk_nm,
          cashIn: isEmpty(item.cash_in) ? null : Number(item.cash_in),
          cashOut: isEmpty(item.cash_out) ? null : Number(item.cash_out),
          cumulativeBalance: isEmpty(item.dpo_amt) ? null : Number(item.dpo_amt),
          note: item.cash_note,
          remarkCode: item.rmrk_cd,
          balanceBegin: isEmpty(item.bal_begin) ? null: Number(item.bal_begin),
          balanceEnd: isEmpty(item.bal_end) ? null : Number(item.bal_end),
          dateBegin: item.date_begin,
          unsettledBuyAmount: isEmpty(item.buy_amt) ? null : Number(item.buy_amt),
          dateEnd: item.date_end,
          description: item.description,
          nextKey: item.next_key,
        })
      );
      return returnResult;
    } else {
      throw new GeneralError(lotteRes.error_desc);
    }
  }

  async initChangeBroker(request: IChangeBrokerRequest, ctx: IContext): Promise<IChangeBrokerResponse> {
    const error = new InvalidParameterError();
    validate(request.accountNumber, 'accountNumber')
      .setRequire()
      .add(validateRequestAccountNoCreator(request))
      .throwValid(error);
    validate(request.newBrokerId, 'newBrokerId')
      .setRequire()
      .throwValid(error);
    validate(request.reason, 'reason')
      .setRequire()
      .throwValid(error);
    error.throwErr();

    const validReasons = getChangeBrokerReasonCodes();
    if (!validReasons.includes(request.reason)) {
      throw new InvalidParameterError().add('INVALID_REASON', 'reason', [request.reason, validReasons.join(', ')]);
    }

    const accountNumber = request.accountNumber.toUpperCase();
    const htsUserId = accountNumber.toLowerCase();

    const existingPendingRequest = await this.accountChangeBrokerRequestRepository.findPendingByAccountNo(accountNumber);
    if (existingPendingRequest) {
      throw new GeneralError(`${Constants.CHANGE_BROKER_PENDING_EXISTS}`);
    }

    const accountInfoRequest: ILotteAccountInfoRequest = {
      acnt_no: accountNumber,
    };
    
    const accountInfoRes = await this.lotteAccountDao.getAccountInfo(accountInfoRequest, ctx);
    if (accountInfoRes.error_code !== '0000' || !accountInfoRes.data_list || accountInfoRes.data_list.length === 0) {
      throw new GeneralError(`${Constants.ACCOUNT_INFO}${accountInfoRes.error_code}`);
    }
    
    const accountInfo = accountInfoRes.data_list[0];
    const previousBrokerId = accountInfo.emp_no || '';
    const accountName = accountInfo.customer_name || '';
    const userEmail = accountInfo.email || '';
    const userPhone = accountInfo.phone || '';

    const lotteRequest: ILotteChangeBrokerRequest = {
      account_number: accountNumber,
      account_name: accountName,
      previous_broker: previousBrokerId,
      new_broker: request.newBrokerId,
      reason: request.reason || '',
      hts_user_id: htsUserId,
    };

    const lotteRes: ILotteChangeBrokerResponse = await this.lotteAccountDao.requestChangeBroker(lotteRequest, ctx);
    const { codes } = parseMessages(lotteRes.error_desc, lotteRes.error_code);

    if (lotteRes.error_code === '0000') {
      const lotteResData: ILotteChangeBrokerData = getElementAtIndex<ILotteChangeBrokerData>(lotteRes.data_list);

      const expiredAt = calculateExpiredDate(5);

      const changeBrokerRequest = new AccountChangeBrokerRequest();
      changeBrokerRequest.coreSeqNo = lotteResData.seq_no;
      changeBrokerRequest.accountNo = accountNumber;
      changeBrokerRequest.customerName = accountName;
      changeBrokerRequest.oldBrokerId = previousBrokerId;
      changeBrokerRequest.newBrokerId = request.newBrokerId;
      changeBrokerRequest.reason = request.reason;
      changeBrokerRequest.userNote = request.note;
      changeBrokerRequest.status = ChangeBrokerStatus.PENDING;
      changeBrokerRequest.expiredAt = expiredAt;

      let previousBrokerName = '';
      let previousBrokerEmail = '';
      let previousBrokerPhone = '';
      let newBrokerName = '';
      let newBrokerEmail = '';

      if (previousBrokerId) {
        const empInfoRequest: ILotteEmployeeInfoRequest = {
          employee_id: previousBrokerId,
        };

        try {
          const empInfoRes = await this.lotteAccountDao.getEmployeeInfo(empInfoRequest, ctx);
          if (empInfoRes.error_code === '0000' && empInfoRes.data_list && empInfoRes.data_list.length > 0) {
            const previousBrokerInfo = empInfoRes.data_list[0];
            previousBrokerName = previousBrokerInfo.os_user_nm || '';
            previousBrokerEmail = previousBrokerInfo.os_email || '';
            previousBrokerPhone = previousBrokerInfo.os_home_phone || '';
          }
        } catch (empError) {
          Logger.warn(ctx.id, `Failed to get employee info for emp_no ${previousBrokerId}:`, empError);
        }
      }

      let newBrokerPhone = '';
      let newBrokerManagerId = '';
      let newBrokerManagerEmail = '';

      if (request.newBrokerId) {
        const newEmpInfoRequest: ILotteEmployeeInfoRequest = {
          employee_id: request.newBrokerId,
        };

        try {
          const newEmpInfoRes = await this.lotteAccountDao.getEmployeeInfo(newEmpInfoRequest, ctx);
          if (newEmpInfoRes.error_code === '0000' && newEmpInfoRes.data_list && newEmpInfoRes.data_list.length > 0) {
            const newBrokerInfo = newEmpInfoRes.data_list[0];
            newBrokerName = newBrokerInfo.os_user_nm || '';
            newBrokerEmail = newBrokerInfo.os_email || '';
            newBrokerPhone = newBrokerInfo.os_home_phone || '';
            newBrokerManagerId = newBrokerInfo.os_mng_emp_no || '';
          }
        } catch (empError) {
          Logger.warn(ctx.id, `Failed to get employee info for new broker ${request.newBrokerId}:`, empError);
        }

        if (newBrokerManagerId) {
          const managerInfoRequest: ILotteEmployeeInfoRequest = {
            employee_id: newBrokerManagerId,
          };

          try {
            const managerInfoRes = await this.lotteAccountDao.getEmployeeInfo(managerInfoRequest, ctx);
            if (managerInfoRes.error_code === '0000' && managerInfoRes.data_list && managerInfoRes.data_list.length > 0) {
              const managerInfo = managerInfoRes.data_list[0];
              newBrokerManagerEmail = managerInfo.os_email || '';
            }
          } catch (empError) {
            Logger.warn(ctx.id, `Failed to get employee info for new broker manager ${newBrokerManagerId}:`, empError);
          }
        }
      }

      changeBrokerRequest.oldBrokerName = previousBrokerName;
      changeBrokerRequest.newBrokerName = newBrokerName;

      await this.accountChangeBrokerRequestRepository.save(changeBrokerRequest);

      try {
        const locale = ctx.orgMsg?.data?.headers?.['accept-language'] || 'vi';
        const createdAt = formatDateVietnam(new Date());
        const reasonLabel = getChangeBrokerReasonLabel(request.reason);

        const initTemplateData = {
          accountNumber,
          accountName,
          previousBrokerName,
          newBrokerName,
          reasonLabel,
          createdAt,
          sequence: lotteResData.seq_no,
        };

        if (newBrokerEmail) {
          sendEmailNotification({
            txId: ctx.txId,
            logId: ctx.id,
            toList: [newBrokerEmail],
            subject: `[NHSV] Yêu cầu đổi người chăm sóc tài khoản - Tài khoản ${accountNumber}`,
            templateName: 'nhsv_change_broker_init',
            templateData: initTemplateData,
            locale,
            logMessage: 'Change broker init notification sent to new broker',
          });
        } else {
          Logger.warn(ctx.id, `No email found for new broker ${request.newBrokerId}, skipping broker email notification`);
        }

        if (newBrokerManagerEmail) {
          sendEmailNotification({
            txId: ctx.txId,
            logId: ctx.id,
            toList: [newBrokerManagerEmail],
            subject: `[NHSV] Yêu cầu đổi người chăm sóc tài khoản - Tài khoản ${accountNumber}`,
            templateName: 'nhsv_change_broker_init',
            templateData: initTemplateData,
            locale,
            logMessage: 'Change broker init notification sent to new broker manager',
          });
        } else {
          Logger.warn(ctx.id, `No email found for new broker manager ${newBrokerManagerId}, skipping manager email notification`);
        }

        if (newBrokerPhone) {
          sendSmsNotification({
            txId: ctx.txId,
            logId: ctx.id,
            phoneNumber: newBrokerPhone,
            templateName: 'nhsv_change_broker_new_broker_sms',
            templateData: {},
            locale,
            logMessage: 'Change broker init SMS sent to new broker',
          });
        } else {
          Logger.warn(ctx.id, `No phone found for new broker ${request.newBrokerId}, skipping broker SMS notification`);
        }

        if (previousBrokerEmail) {
          sendEmailNotification({
            txId: ctx.txId,
            logId: ctx.id,
            toList: [previousBrokerEmail],
            subject: `[NHSV] Thông báo chuyển đổi người chăm sóc tài khoản ${accountNumber}`,
            templateName: 'nhsv_change_broker_old_broker',
            templateData: initTemplateData,
            locale,
            logMessage: 'Change broker init notification sent to old broker',
          });
        } else {
          Logger.warn(ctx.id, `No email found for old broker ${previousBrokerId}, skipping old broker email notification`);
        }

        if (previousBrokerPhone) {
          sendSmsNotification({
            txId: ctx.txId,
            logId: ctx.id,
            phoneNumber: previousBrokerPhone,
            templateName: 'nhsv_change_broker_old_broker_sms',
            templateData: {},
            locale,
            logMessage: 'Change broker init SMS sent to old broker',
          });
        } else {
          Logger.warn(ctx.id, `No phone found for old broker ${previousBrokerId}, skipping old broker SMS notification`);
        }

        if (userEmail) {
          sendEmailNotification({
            txId: ctx.txId,
            logId: ctx.id,
            toList: [userEmail],
            subject: '[NHSV] Xác nhận đã nhận yêu cầu đổi người chăm sóc tài khoản',
            templateName: 'nhsv_change_broker_customer_init',
            templateData: initTemplateData,
            locale,
            logMessage: 'Change broker init notification sent to customer',
          });
        } else {
          Logger.warn(ctx.id, `No email found for customer ${accountNumber}, skipping customer notification`);
        }

        if (userPhone) {
          sendSmsNotification({
            txId: ctx.txId,
            logId: ctx.id,
            phoneNumber: userPhone,
            templateName: 'nhsv_change_broker_init_sms',
            templateData: { accountNumber },
            locale,
            logMessage: 'Change broker init SMS sent to customer',
          });
        } else {
          Logger.warn(ctx.id, `No phone found for customer ${accountNumber}, skipping SMS notification`);
        }
      } catch (notificationError) {
        Logger.error(ctx.id, 'Failed to send change broker init notification:', notificationError);
      }

      const response: IChangeBrokerResponse = {
        sequence: lotteResData.seq_no,
        newBrokerId: request.newBrokerId,
      };

      return response;
    } else {
      throw new GeneralError(`${Constants.CHANGE_BROKER_INIT}${codes}`);
    }
  }

  async getChangeBrokerHistory(
    request: IChangeBrokerHistoryRequest,
    ctx: IContext
  ): Promise<IChangeBrokerHistoryItemResponse[]> {
    const error = new InvalidParameterError();
    validate(request.accountNumber, 'accountNumber')
      .setRequire()
      .add(validateRequestAccountNoCreator(request))
      .throwValid(error);
    error.throwErr();

    const accountNumber = request.accountNumber.toUpperCase();

    let status: ChangeBrokerStatus | undefined;
    if (request.status) {
      switch (request.status.toUpperCase()) {
        case 'PENDING':
          status = ChangeBrokerStatus.PENDING;
          break;
        case 'APPROVED':
          status = ChangeBrokerStatus.APPROVED;
          break;
        case 'REJECTED':
          status = ChangeBrokerStatus.REJECTED;
          break;
        default:
          break;
      }
    }

    const today = new Date();
    const startOfToday = new Date(today.getFullYear(), today.getMonth(), today.getDate(), 0, 0, 0, 0);
    const endOfToday = new Date(today.getFullYear(), today.getMonth(), today.getDate(), 23, 59, 59, 999);
    
    let fromDate: Date;
    if (request.fromDate) {
      fromDate = convertStringToDate(request.fromDate, DATE_DISPLAY_FORMAT);
    } else {
      fromDate = startOfToday;
    }

    let toDate: Date;
    if (request.toDate) {
      const parsedToDate = convertStringToDate(request.toDate, DATE_DISPLAY_FORMAT);
      toDate = new Date(parsedToDate.getFullYear(), parsedToDate.getMonth(), parsedToDate.getDate(), 23, 59, 59, 999);
    } else {
      toDate = endOfToday;
    }
    const fetchCount = request.fetchCount && request.fetchCount > 0 ? request.fetchCount : config.defaultFetchCount;
    const nextKey = request.nextKey ? request.nextKey.trim() : undefined;

    const dbRecords = await this.accountChangeBrokerRequestRepository.findHistoryByAccountNo(
      accountNumber,
      status,
      fromDate,
      toDate,
      fetchCount,
      nextKey
    );

    const hasMore = dbRecords.length > fetchCount;
    const records = hasMore ? dbRecords.slice(0, fetchCount) : dbRecords;

    return records.map((record, index) => {
      const isLastItem = index === records.length - 1;
      return {
        sequence: Number(record.coreSeqNo) || record.id,
        previousBrokerId: record.oldBrokerId || '',
        previousBrokerName: record.oldBrokerName || '',
        newBrokerId: record.newBrokerId,
        newBrokerName: record.newBrokerName || '',
        reason: record.reason || '',
        status: record.status,
        requestedDate: record.createdAt ? formatDateToDisplay(record.createdAt, DATE_DISPLAY_FORMAT) : '',
        updatedDate: record.updatedAt ? formatDateToDisplay(record.updatedAt, DATE_DISPLAY_FORMAT) : '',
        nextKey: isLastItem && hasMore ? record.id.toString() : undefined,
      };
    });
  }

  async registerBankAccount(
    request: IRegisterBankAccountRequest,
    ctx: IContext
  ): Promise<IRegisterBankAccountResponse> {
    const error = new InvalidParameterError();
    validate(request.accountNumber, 'accountNumber')
      .setRequire()
      .add(validateRequestAccountNoCreator(request))
      .throwValid(error);
    validate(request.bankCode, 'bankCode')
      .setRequire()
      .throwValid(error);
    validate(request.bankAccountNumber, 'bankAccountNumber')
      .setRequire()
      .throwValid(error);
    validate(request.branchCode, 'branchCode')
      .setRequire()
      .throwValid(error);
    error.throwErr();

    const accountNumber = request.accountNumber.toUpperCase();
    const userData = request.headers.token.userData;
    const bankAccountName = userData['name'] || '';
    const htsUserId = userData.username || accountNumber.toLowerCase();

    const lotteRequest: ILotteRegisterBankAccountRequest = {
      acnt_no: accountNumber,
      bank_cd: request.bankCode,
      bank_acnt_no: request.bankAccountNumber,
      bank_acnt_nm: bankAccountName,
      branch: request.branchCode,
      hts_user_id: htsUserId,
    };

    const lotteRes = await this.lotteAccountDao.registerBankAccount(lotteRequest, ctx);

    if (lotteRes.error_code === '0000' && lotteRes.success) {
      return { success: true };
    } else {
      throw new GeneralError(lotteRes.error_desc);
    }
  }

  async deleteBankAccount(
    request: IDeleteBankAccountRequest,
    ctx: IContext
  ): Promise<IDeleteBankAccountResponse> {
    const error = new InvalidParameterError();
    validate(request.accountNumber, 'accountNumber')
      .setRequire()
      .add(validateRequestAccountNoCreator(request))
      .throwValid(error);
    validate(request.bankCode, 'bankCode')
      .setRequire()
      .throwValid(error);
    validate(request.bankAccountNumber, 'bankAccountNumber')
      .setRequire()
      .throwValid(error);
    validate(request.branchCode, 'branchCode')
      .setRequire()
      .throwValid(error);
    error.throwErr();

    const accountNumber = request.accountNumber.toUpperCase();
    const userData = request.headers.token.userData;
    const bankAccountName = userData['name'] || '';
    const htsUserId = userData.username || accountNumber.toLowerCase();

    const lotteRequest: ILotteDeleteBankAccountRequest = {
      acnt_no: accountNumber,
      bank_cd: request.bankCode,
      bank_acnt_no: request.bankAccountNumber,
      bank_acnt_nm: bankAccountName,
      branch: request.branchCode,
      hts_user_id: htsUserId,
    };

    const lotteRes = await this.lotteAccountDao.deleteBankAccount(lotteRequest, ctx);

    if (lotteRes.error_code === '0000' && lotteRes.success) {
      return { success: true };
    } else {
      throw new GeneralError(lotteRes.error_desc);
    }
  }
}