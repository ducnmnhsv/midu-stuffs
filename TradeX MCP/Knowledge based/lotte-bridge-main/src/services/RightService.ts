import { Inject, Service } from 'typedi';
import { LotteBalanceDao } from '../daos/LotteBalanceDao';
import { Errors, Utils } from 'tradex-common';
import { IContext } from '../models/IContext';
import {
  getBankCode,
  getElementAtIndex,
  parseMessages,
  setDefault,
  validateRequestAccountNoCreator,
  validateSubAccount,
} from '../utils/lotte';
import config from '../config';
import { Constants } from '../constants/Constants';
import { IRightAvailableRequest } from '../models/request/IRightAvailableRequest';
import { ILotteRightAvailableRequest } from '../models/request/lotte/ILotteRightAvailableRequest';
import { MARKET_TYPE, RIGHT_TYPE } from '../constants/enum';
import {
  ILotteRightAvailableResponse,
  ILotteRightAvailableResponseData,
} from '../models/response/lotte/ILotteRightAvailableResponse';
import { IRightAvailableResponse } from '../models/response/IRightAvailableResponse';
import { IRightDetailResponse } from '../models/response/IRightDetailResponse';
import { IRightDetailRequest } from '../models/request/IRightDetailRequest';
import { ILotteRightDetailRequest } from '../models/request/lotte/ILotteRightDetailRequest';
import {
  ILotteRightDetailResponse,
  ILotteRightDetailResponseData,
} from '../models/response/lotte/ILotteRightDetailResponse';
import { IRightHistoryRequest } from '../models/request/IRightHistoryRequest';
import { IRegisterRightRequest } from '../models/request/IRegisterRightRequest';
import { ICancelRightRequest } from '../models/request/ICancelRightRequest';
import { IRightHistoryResponse } from '../models/response/IRightHistoryResponse';
import { IParam } from '../models/request/lotte/ILotteRequest';
import { ILotteRightHistoryRequest } from '../models/request/lotte/ILotteRightHistoryRequest';
import {
  ILotteRightHistoryResponse,
  ILotteRightHistoryResponseData,
  ILotteRightHistoryResponseDataListItems,
} from '../models/response/lotte/ILotteRightHistoryResponse';
import { ILotteCancelRightRequest } from '../models/request/lotte/ILotteCancelRightRequest';
import { ILotteCancelRightResponse } from '../models/response/lotte/ILotteCancelRightResponse';
import { ILotteRegisterRightRequest } from '../models/request/lotte/ILotteRegisterRightRequest';
import { ILotteRegisterRightResponse } from '../models/response/lotte/ILotteRegisterRightResponse';
import { InjectRepository } from 'typeorm-typedi-extensions';
import { AccountBankInfoRepository } from '../repositories/AccountBankInfoRepository';
import { IUpcomingRightRequest } from '../models/request/IUpcomingRightRequest';
import { IUpcomingRightItem, IUpcomingRightResponse } from '../models/response/IUpcomingRightResponse';
import { ILotteUpcomingRightRequest } from '../models/request/lotte/ILotteUpcomingRightRequest';
import {
  ILotteUpcomingRightResponse,
  ILotteUpcomingRightResponseData,
} from '../models/response/lotte/ILotteUpcomingRightResponse';

const { InvalidParameterError, GeneralError } = Errors;
const { validate } = Utils;

@Service()
export class RightService {
  @Inject()
  private lotteBalanceDao: LotteBalanceDao;
  @InjectRepository()
  private accountBankInfoRepository: AccountBankInfoRepository;

  async rightAvailable(request: IRightAvailableRequest, ctx: IContext): Promise<IRightAvailableResponse[]> {
    const error = new InvalidParameterError();
    validate(request.accountNumber, 'accountNumber')
      .setRequire()
      .add(validateRequestAccountNoCreator(request))
      .throwValid(error);
    error.throwErr();
    const nextKey: string =
      setDefault<string>(request.lastBaseDate, '') +
      setDefault<string>(request.lastStockCode, '').padStart(20, '0') +
      setDefault<string>(request.lastSequenceNumber, '').padEnd(2, '0');
    const lotteRequest: ILotteRightAvailableRequest = {
      rgt_tp: setDefault<string>(request.rightType, RIGHT_TYPE.Subscription),
      acnt_no: request.accountNumber.toUpperCase(),
      sub_no: setDefault<string>(request.subNumber, config.defaultSubNumber),
      next_key: nextKey,
      row_count: setDefault<number>(request.fetchCount, config.defaultFetchCount).toString(),
    };

    const lotteRes: ILotteRightAvailableResponse = await this.lotteBalanceDao.rightAvailable(lotteRequest, ctx);
    const { codes, messages } = parseMessages(lotteRes.error_desc, lotteRes.error_code);
    if (codes === null || codes === '0011') {
      return lotteRes.data_list.map(
        (item: ILotteRightAvailableResponseData): IRightAvailableResponse => ({
          stockCode: item.stk_cd,
          stockName: item.stk_nm,
          sequenceNumber: item.seq,
          baseDate: item.base_date,
          rightStatus: item.proc_nm,
          startDate: item.start_dt,
          endDate: item.end_dt,
          issuePrice: item.right_price,
          availableQuantity: item.avai_stk_qty,
          note: item.cnte,
        })
      );
    } else if (codes !== null && codes === '2016') {
      return [];
    } else {
      throw new GeneralError(`${Constants.RIGHTS_AVAILABLE}${messages}`);
    }
  }

  async rightDetail(request: IRightDetailRequest, ctx: IContext): Promise<IRightDetailResponse[]> {
    const error = new InvalidParameterError();
    validate(request.accountNumber, 'accountNumber')
      .setRequire()
      .add(validateRequestAccountNoCreator(request))
      .throwValid(error);
    validate(request.stockCode, 'stockCode')
      .setRequire()
      .throwValid(error);
    validate(request.baseDate, 'baseDate')
      .setRequire()
      .throwValid(error);
    validate(request.rightType, 'rightType')
      .setRequire()
      .throwValid(error);
    validate(request.sequenceNumber, 'sequenceNumber')
      .setRequire()
      .throwValid(error);
    error.throwErr();
    const lotteRequest: ILotteRightDetailRequest = {
      trade_date: request.baseDate,
      stk_cd: request.stockCode,
      acnt_no: request.accountNumber.toUpperCase(),
      sub_no: setDefault<string>(request.subNumber, config.defaultSubNumber),
      rgt_tp: request.rightType,
      seq_no: `${request.sequenceNumber}`,
    };
    const lotteRes: ILotteRightDetailResponse = await this.lotteBalanceDao.rightDetail(lotteRequest, ctx);
    const { codes, messages } = parseMessages(lotteRes.error_desc, lotteRes.error_code);
    if (codes === null || codes === '0011') {
      return lotteRes.data_list.map(
        (item: ILotteRightDetailResponseData): IRightDetailResponse => ({
          tradeNumber: item.outamt_trd_no,
          startDate: item.sbst_st_dt,
          endDate: item.sbst_lst_dt,
          standardQuantity: item.own_qty,
          issuePrice: item.rgt_iss_pri,
          availableQuantity: item.inq_qty,
          availableAmount: item.cons_sbst_able_amt,
          approveWaitingQuantity: item.waiting_qty,
          bankCancelWaitingQuantity: item.cancel_qty,
          processStatusCode: item.rgt_proc_stat,
          processStatusName: item.proc_nm,
          quantity: `${item.asn_qty}`,
        })
      );
    } else if (codes !== null && codes === '2016') {
      return [];
    } else {
      throw new GeneralError(`${Constants.RIGHTS_AVAILABLE}${messages}`);
    }
  }

  async rightHistory(request: IRightHistoryRequest, ctx: IContext): Promise<IRightHistoryResponse[]> {
    const nextKey: string = !request.lastBaseDate
      ? '0'
      : request.lastBaseDate +
        setDefault<string>(request.lastStockCode, '').padEnd(20, '0') +
        setDefault<string>(request.lastSequenceNumber, '').padStart(2, '0');
    const lotteRequest: ILotteRightHistoryRequest = {
      mkt_tp: setDefault<string>(request.marketType, MARKET_TYPE.ALL),
      acnt_no: setDefault<string>(request.accountNumber, request.headers.token.userData.username).toUpperCase(),
      sub_no: setDefault<string>(request.subNumber, config.defaultSubNumber),
      stk_cd: setDefault<string>(request.stockCode, config.defaultStockCode),
      next_key: nextKey,
      row_count: setDefault<number>(request.fetchCount, config.defaultFetchCount).toString(),
    };
    const lotteRes: ILotteRightHistoryResponse = await this.lotteBalanceDao.rightHistory(lotteRequest, ctx);
    const { codes, messages } = parseMessages(lotteRes.error_desc, lotteRes.error_code);
    if (codes === null || codes === '0011') {
      const lotteResDataList: ILotteRightHistoryResponseData = getElementAtIndex<ILotteRightHistoryResponseData>(
        lotteRes.data_list
      );
      if (lotteResDataList.listItems != null) {
        return lotteResDataList.listItems.map(
          (item: ILotteRightHistoryResponseDataListItems): IRightHistoryResponse => ({
            stockCode: item.symbol,
            sequenceNumber: Number(item.seq),
            baseDate: item.base_date,
            status: item.status,
            baseRate: Number(item.base_rate),
            dividendRate: Number(item.divd_rate),
            ownQty: Number(item.own_qtty),
            beginDate: item.begin_date,
            endDate: item.end_date,
            issuePrice: Number(item.issue_price),
            availableQty: Number(item.avail_qtty),
            requestQty: Number(item.req_qtty),
            requestAmount: Number(item.req_amt),
            effectiveDate: item.effect_date,
            isEffective: item.effect_yn,
          })
        );
      }
      return [];
    } else if (codes !== null && codes === '2016') {
      return [];
    } else {
      throw new GeneralError(`${Constants.RIGHTS_HISTORY}${messages}`);
    }
  }

  async registerRight(request: IRegisterRightRequest, ctx: IContext): Promise<IParam> {
    const grantType: string = request.headers?.token?.grantType;
    const error = new InvalidParameterError();
    validate(request.accountNumber, 'accountNumber')
      .setRequire()
      .add(validateRequestAccountNoCreator(request))
      .throwValid(error);
    validate(request.stockCode, 'stockCode')
      .setRequire()
      .throwValid(error);
    validate(request.quantity, 'quantity')
      .setRequire()
      .throwValid(error);
    validate(request.amount, 'amount')
      .setRequire()
      .throwValid(error);
    validate(request.tradeNumber, 'tradeNumber')
      .setRequire()
      .throwValid(error);
    validate(request.rightType, 'rightType')
      .setRequire()
      .throwValid(error);
    validate(request.sequenceNumber, 'sequenceNumber')
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
    const lotteRequest: ILotteRegisterRightRequest = {
      dept_no1: request.headers.token.userData.deptCode,
      rgt_std_dt: setDefault<string>(request.baseDate, ''),
      stk_cd: request.stockCode,
      acnt_no: request.accountNumber.toUpperCase(),
      sub_no: subNumber,
      cons_sbst_qty: `${request.quantity}`,
      cons_sbst_amt: `${request.amount}`,
      org_seq_no: `${request.tradeNumber}`,
      seq_no: `${request.sequenceNumber}`,
      bank_cd: bankCode,
      bank_acc: request.bankAccount,
      rgt_tp: request.rightType,
    };
    const lotteRes: ILotteRegisterRightResponse = await this.lotteBalanceDao.registerRight(lotteRequest, ctx);
    const { codes, messages } = parseMessages(lotteRes.error_desc, lotteRes.error_code);
    if (codes === null || codes === '0405') {
      return {
        message: messages,
      };
    } else {
      throw new GeneralError(`${Constants.RIGHTS_REGISTER}${messages}`);
    }
  }

  async cancelRight(request: ICancelRightRequest, ctx: IContext): Promise<IParam> {
    const grantType: string = request.headers?.token?.grantType;
    const error = new InvalidParameterError();
    validate(request.baseDate, 'baseDate')
      .setRequire()
      .throwValid(error);
    validate(request.stockCode, 'stockCode')
      .setRequire()
      .throwValid(error);
    validate(request.accountNumber, 'accountNumber')
      .setRequire()
      .add(validateRequestAccountNoCreator(request))
      .throwValid(error);
    validate(request.quantity, 'quantity')
      .setRequire()
      .throwValid(error);
    validate(request.amount, 'amount')
      .setRequire()
      .throwValid(error);
    validate(request.tradeNumber, 'tradeNumber')
      .setRequire()
      .throwValid(error);
    validate(request.sequenceNumber, 'sequenceNumber')
      .setRequire()
      .throwValid(error);
    validate(request.rightType, 'rightType')
      .setRequire()
      .throwValid(error);
    if (grantType !== 'client_credentials') {
      validate(request.bankCode, 'bankCode')
        .setRequire()
        .throwValid(error);
    }
    validate(request.subNumber, 'subNumber')
      .add(validateSubAccount(request, false))
      .throwValid(error);
    error.throwErr();
    const subNumber: string = setDefault<string>(request.subNumber, config.defaultSubNumber);
    let bankCode: string = request.bankCode;
    if (Utils.isEmpty(bankCode) && grantType === 'client_credentials') {
      const bankInfo: { [key: string]: string[] } = request.headers.token.userData['bankInfo'];
      bankCode = await getBankCode(request.accountNumber, subNumber, bankInfo, this.accountBankInfoRepository);
    }
    const lotteRequest: ILotteCancelRightRequest = {
      dept_no1: request.headers.token.userData.deptCode,
      rgt_std_dt: request.baseDate,
      stk_cd: request.stockCode,
      acnt_no: request.accountNumber.toUpperCase(),
      sub_no: subNumber,
      cons_sbst_qty: `${request.quantity}`,
      cons_sbst_amt: `${request.amount}`,
      org_seq_no: `${request.tradeNumber}`,
      seq_no: `${request.sequenceNumber}`,
      bank_cd: bankCode,
      bank_acc: request.bankAccount,
      rgt_tp: request.rightType,
    };
    const lotteRes: ILotteCancelRightResponse = await this.lotteBalanceDao.cancelRight(lotteRequest, ctx);
    const { codes, messages } = parseMessages(lotteRes.error_desc, lotteRes.error_code);
    if (codes === null || codes === '0209') {
      return {
        message: messages,
      };
    } else {
      throw new GeneralError(`${Constants.RIGHTS_CANCEL}${messages}`);
    }
  }

  async upcomingRights(request: IUpcomingRightRequest, ctx: IContext): Promise<IUpcomingRightResponse> {
    const error = new InvalidParameterError();
    validate(request.accountNo, 'accountNo')
      .setRequire()
      .add(validateRequestAccountNoCreator(request))
      .throwValid(error);
    validate(request.subNo, 'subNo')
      .setRequire()
      .throwValid(error);
    error.throwErr();

    const lotteRequest: ILotteUpcomingRightRequest = {
      acnt_no: request.accountNo.toUpperCase(),
      sub_no: request.subNo,
      next_key: setDefault<string>(request.nextKey, '0'),
      row_count: setDefault<number>(request.fetchCount, config.defaultFetchCount),
    };

    const lotteResponse: ILotteUpcomingRightResponse = await this.lotteBalanceDao.upcomingRights(lotteRequest, ctx);
    const { codes } = parseMessages(lotteResponse.error_desc, lotteResponse.error_code);

    if (codes === null || codes === '0011') {
      const lotteResData: ILotteUpcomingRightResponseData = getElementAtIndex<ILotteUpcomingRightResponseData>(
        lotteResponse.data_list
      );

      if (lotteResData.listItems) {
        return lotteResData.listItems.map(
          (item): IUpcomingRightItem => ({
            symbol: item.stk_cd,
            rightType: item.rgt_tp,
            baseDate: item.rgt_std_dt,
            quantity: Number(item.qty),
            inquiryDate: item.inq_dt,
            oddLotAmount: Number(item.flotq_amt),
            oddLotPaidDate: item.flotq_dt,
            receivedAmount: Number(item.asn_amt),
            dividendPayDate: item.divi_pay_dt,
            nextKey: item.next_key,
          })
        );
      }
      return [];
    } else if (codes !== null && codes === '2016') {
      return [];
    } else {
      throw new GeneralError(lotteResponse.error_desc);
    }
  }
}
