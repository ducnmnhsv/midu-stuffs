import { Inject, Service } from 'typedi';
import { IContext } from '../models/IContext';
import { LotteOrderDao } from '../daos/LotteOrderDao';
import { Errors, Logger, Utils } from 'tradex-common';
import { ILotteAdvancedOrderRequest, ILotteNornalOrderRequest } from '../models/request/lotte/ILotteEnterOrderRequest';
import {
  MARKET_TYPE,
  MATCH_TYPE,
  ORDER_TYPE,
  SELL_BUY_TYPE_LOTTE,
  SELL_BUY_TYPE,
  SORT_TYPE,
  SortType,
  ORDER_STATUS,
  getKeyByValue,
  ORDER_MODIFY_CANCEL_TYPE,
  CHANNEL_TYPE,
  MARKET_TYPE_LOTTE,
  MARKET_TYPE_ADVANCED,
  SELL_BUY_TYPE_ADVANCED,
  LOTTE_LANG_CODE,
} from '../constants/enum';
import { ILotteModifyOrderRequest } from '../models/request/lotte/ILotteModifyOrderRequest';
import { IHistoryOrderAdvancedRequest, IHistoryOrderRequest } from '../models/request/IHistoryOrderRequest';
import {
  ILotteHistoryOrderAdvancedRequest,
  ILotteHistoryOrderRequest,
} from '../models/request/lotte/ILotteHistoryOrderRequest';
import config from '../config';
import {
  ILotteHistoryOrderAdvancedResponse,
  ILotteHistoryOrderResponse,
  ILotteHistoryOrderResponseData,
} from '../models/response/lotte/ILotteHistoryOrderResponse';
import { IHistoryOrderAdvancedResponse, IHistoryOrderResponse } from '../models/response/IHistoryOrderResponse';
import {
  getBankCode,
  getElementAtIndex,
  getPlatformValueCore,
  parseMessages,
  setDefault,
  validateRequestAccountNoCreator,
  validateSubAccount,
} from '../utils/lotte';
import { ITodayUnmatchOrderRequest } from '../models/request/ITodayUnmatchOrderRequest';
import { ILotteTodayUnmatchOrderRequest } from '../models/request/lotte/ILotteTodayUnmatchOrderRequest';
import {
  ILotteTodayUnmatchOrderResponse,
  ILotteTodayUnmatchOrderResponseData,
} from '../models/response/lotte/ILotteTodayUnmatchOrderResponse';
import { ITodayUnmatchOrderResponse } from '../models/response/ITodayUnmatchOrderResponse';
import { IOrderConfirmHistoryRequest, IOrderConfirmRequest } from '../models/request/IOrderConfirmRequest';
import { IOrderConfirmHistoryResponse, IOrderConfirmResponse } from '../models/response/IOrderConfirmResponse';
import {
  ILotteOrderConfirmHistoryRequest,
  ILotteOrderConfirmRequest,
} from '../models/request/lotte/ILotteOrderConfirmRequest';
import {
  ILotteOrderConfirmHistoryResponse,
  ILotteOrderConfirmHistoryResponseData,
  ILotteOrderConfirmResponse,
} from '../models/response/lotte/ILotteOrderConfirmResponse';
import { IAdvancedOrderResponse, INormalOrderResponse } from '../models/response/IEnterOrderResponse';
import {
  ILotteAdvancedOrderResponse,
  ILotteAdvancedOrderResponseData,
  ILotteNormalOrderResponse,
  ILotteNormalOrderResponseData,
} from '../models/response/lotte/ILotteEnterOrderResponse';
import { IAdvancedOrderRequest, INormalOrderRequest } from '../models/request/IEnterOrderRequest';
import { ICancelOrderAdvancedResponse, ICancelOrderNormalResponse } from '../models/response/ICancelOrderResponse';
import { ICancelOrderAdvancedRequest, ICancelOrderNormalRequest } from '../models/request/ICancelOrderRequest';
import {
  ILotteCancelOrderAdvancedRequest,
  ILotteCancelOrderNormalRequest,
} from '../models/request/lotte/ILotteCancelOrderRequest';
import {
  ILotteCancelAdvancedOrderResponseData,
  ILotteCancelNormalOrderResponseData,
  ILotteCancelOrderAdvancedResponse,
  ILotteCancelOrderNormalResponse,
} from '../models/response/lotte/ILotteCancelOrderResponse';
import { IModifyOrderNormalRequest } from '../models/request/IModifyOrderRequest';
import { IModifyOrderNormalResponse } from '../models/response/IModifyNormalOrderResponse';
import {
  ILotteModifyOrderNormalResponse,
  ILotteModifyOrderNormalResponseData,
} from '../models/response/lotte/ILotteModifyNormalOrderResponse';
import { Constants } from '../constants/Constants';
import { IOrderBookRequest } from '../models/request/IOrderBookRequest';
import { IOrderBookResponse } from '../models/response/IOrderBookResponse';
import { ILotteOrderBookRequest } from '../models/request/lotte/ILotteOrderBookRequest';
import { ILotteOrderBookResponse, ILotteOrderBookResponseData } from '../models/response/lotte/ILotteOrderBookResponse';
import { checkStringTrim } from '../utils/defaultUtils';
import { InjectRepository } from 'typeorm-typedi-extensions';
import { AccountBankInfoRepository } from '../repositories/AccountBankInfoRepository';

const { GeneralError, InvalidParameterError } = Errors;
const { validate, formatDateToDisplay, DATE_DISPLAY_FORMAT } = Utils;

@Service()
export class OrderService {
  @Inject()
  private lotteOrderDao: LotteOrderDao;
  @InjectRepository()
  private accountBankInfoRepository: AccountBankInfoRepository;

  async enterNormalOrder(request: INormalOrderRequest, ctx: IContext): Promise<INormalOrderResponse> {
    const grantType: string = request.headers?.token?.grantType;
    const error = new InvalidParameterError();
    validate(request.accountNumber, 'accountNumber')
      .setRequire()
      .add(validateRequestAccountNoCreator(request))
      .throwValid(error);
    validate(request.subNumber, 'subNumber')
      .setRequire()
      .add(validateSubAccount(request, true))
      .throwValid(error);
    validate(request.stockCode, 'stockCode')
      .setRequire()
      .throwValid(error);
    validate(request.orderQuantity, 'orderQuantity')
      .setRequire()
      .throwValid(error);
    validate(request.orderType, 'orderType')
      .setRequire()
      .throwValid(error);
    validate(request.sellBuyType, 'sellBuyType')
      .setRequire()
      .throwValid(error);
    validate(request.securitiesType, 'securitiesType')
      .setRequire()
      .throwValid(error);
    validate(request.deviceUniqueId, 'deviceUniqueId')
      .setRequire()
      .throwValid(error);
    if (request.orderType === ORDER_TYPE.LO) {
      validate(request.orderPrice, 'orderPrice')
        .setRequire()
        .throwValid(error);
    }
    if (grantType !== 'client_credentials') {
      validate(request.bankCode, 'bankCode')
        .setRequire()
        .throwValid(error);
    }
    error.throwErr();
    let bankCode: string = request.bankCode;
    if (Utils.isEmpty(bankCode) && grantType === 'client_credentials') {
      const bankInfo: { [key: string]: string[] } = request.headers.token.userData['bankInfo'];
      bankCode = await getBankCode(request.accountNumber, request.subNumber, bankInfo, this.accountBankInfoRepository);
    }
    const language = request.headers['accept-language'];
    let platform: string = setDefault<string | null | undefined>(request.channel, request.headers.token.platform);
    if (Utils.isEmpty(platform) && grantType === 'client_credentials') {
      platform = config.platformDifiSoft;
    }
    const lotteRequest: ILotteNornalOrderRequest = {
      hts_user_id: request.headers.token.userData.username,
      hts_user_nm: setDefault<string>(request.name, request.headers.token.userData['name']),
      idno: request.headers.token.userData.identifierNumber,
      acnt_no: request.accountNumber.toUpperCase(),
      sub_no: request.subNumber,
      stk_cd: request.stockCode,
      ord_pri: request.orderPrice,
      ord_qty: request.orderQuantity,
      bank_cd: bankCode,
      stk_ord_tp: ORDER_TYPE[request.orderType],
      cli_ip_addr: request.sourceIp,
      cli_mac_addr: request.deviceUniqueId,
      lang_code:
        language == null || LOTTE_LANG_CODE[language] == null ? config.defaultLanguage : LOTTE_LANG_CODE[language],
      mdm_tp: getPlatformValueCore(platform),
    };
    let lotteRes: ILotteNormalOrderResponse;
    switch (request.sellBuyType) {
      case SELL_BUY_TYPE.BUY:
        lotteRes = await this.lotteOrderDao.enterBuyNormalOrder(lotteRequest, ctx);
        break;
      case SELL_BUY_TYPE.SELL:
        lotteRes = await this.lotteOrderDao.enterSellNormalOrder(lotteRequest, ctx);
        break;
      default:
        throw new InvalidParameterError().add('INVALID_VALUE', 'sellBuyType', [request.sellBuyType]);
    }
    const { codes, messages } = parseMessages(lotteRes.error_desc, lotteRes.error_code);
    Logger.info(ctx.id, 'codes', codes, 'messages', messages);
    const lotteResDataList: ILotteNormalOrderResponseData = getElementAtIndex<ILotteNormalOrderResponseData>(
      lotteRes.data_list
    );
    if (codes === null || codes === '0307' || codes === '0305') {
      const response: INormalOrderResponse = {
        message: lotteRes.error_desc,
        orderNumber: lotteResDataList.new_ord_no,
      };
      return response;
    } else {
      throw new GeneralError(`${Constants.ORDER_PLACE}${codes}`);
    }
  }

  async cancelNormalOrder(request: ICancelOrderNormalRequest, ctx: IContext): Promise<ICancelOrderNormalResponse> {
    const error = new InvalidParameterError();
    validate(request.accountNumber, 'accountNumber')
      .setRequire()
      .add(validateRequestAccountNoCreator(request))
      .throwValid(error);
    validate(request.orderNumber, 'orderNumber')
      .setRequire()
      .throwValid(error);
    validate(request.branchCode, 'branchCode')
      .setRequire()
      .throwValid(error);
    error.throwErr();
    const language: string = request.headers['accept-language'];
    const grantType: string = request.headers?.token?.grantType;
    let platform: string = setDefault<string | null | undefined>(request.channel, request.headers.token.platform);
    if (Utils.isEmpty(platform) && grantType === 'client_credentials') {
      platform = config.platformDifiSoft;
    }
    const lotteRequest: ILotteCancelOrderNormalRequest = {
      hts_user_id: request.headers.token.userData.username,
      hts_user_nm: setDefault<string>(request.name, request.headers.token.userData['name']),
      idno: request.headers.token.userData.identifierNumber,
      acnt_no: request.accountNumber.toUpperCase(),
      sub_no: setDefault<string>(request.subNumber, config.defaultSubNumber),
      ord_no: request.orderNumber,
      bank_cd: request.branchCode,
      cli_ip_addr: request.sourceIp,
      cli_mac_addr: request.deviceUniqueId,
      lang_code:
        language == null || LOTTE_LANG_CODE[language] == null ? config.defaultLanguage : LOTTE_LANG_CODE[language],
      mdm_tp: getPlatformValueCore(platform),
    };
    const lotteRes: ILotteCancelOrderNormalResponse = await this.lotteOrderDao.cancelNormalOrder(lotteRequest, ctx);
    const { codes, messages } = parseMessages(lotteRes.error_desc, lotteRes.error_code);
    Logger.info(ctx.id, 'codes', codes, 'messages', messages);
    const lotteResDataList: ILotteCancelNormalOrderResponseData = getElementAtIndex<
      ILotteCancelNormalOrderResponseData
    >(lotteRes.data_list);
    if (codes === null || codes === '0320') {
      const response: ICancelOrderNormalResponse = {
        orderNumber: lotteResDataList.new_ord_no,
      };
      return response;
    } else {
      throw new GeneralError(`${Constants.ORDER_CANCEL}${codes}`);
    }
  }

  async modifyNormalOrder(request: IModifyOrderNormalRequest, ctx: IContext): Promise<IModifyOrderNormalResponse> {
    const error = new InvalidParameterError();
    validate(request.accountNumber, 'accountNumber')
      .setRequire()
      .add(validateRequestAccountNoCreator(request))
      .throwValid(error);
    validate(request.orderNumber, 'orderNumber')
      .setRequire()
      .throwValid(error);
    validate(request.orderPrice, 'orderPrice')
      .setRequire()
      .throwValid(error);
    validate(request.orderQuantity, 'orderQuantity')
      .setRequire()
      .throwValid(error);
    validate(request.branchCode, 'branchCode')
      .setRequire()
      .throwValid(error);
    error.throwErr();
    const language = request.headers['accept-language'];
    const grantType: string = request.headers?.token?.grantType;
    let platform: string = setDefault<string | null | undefined>(request.channel, request.headers.token.platform);
    if (Utils.isEmpty(platform) && grantType === 'client_credentials') {
      platform = config.platformDifiSoft;
    }
    const lotteRequest: ILotteModifyOrderRequest = {
      hts_user_id: request.headers.token.userData.username,
      hts_user_nm: request.headers.token.userData['name'],
      idno: request.headers.token.userData.identifierNumber,
      acnt_no: request.accountNumber.toUpperCase(),
      sub_no: setDefault<string>(request.subNumber, config.defaultSubNumber),
      ord_no: request.orderNumber,
      ord_pri: request.orderPrice,
      ord_qty: request.orderQuantity,
      brch_cd: request.branchCode,
      cli_ip_addr: request.sourceIp,
      cli_mac_addr: request.deviceUniqueId,
      lang_code:
        language == null || LOTTE_LANG_CODE[language] == null ? config.defaultLanguage : LOTTE_LANG_CODE[language],
      mdm_tp: getPlatformValueCore(platform),
    };
    const lotteRes: ILotteModifyOrderNormalResponse = await this.lotteOrderDao.modifyNormalOrder(lotteRequest, ctx);
    const { codes, messages } = parseMessages(lotteRes.error_desc, lotteRes.error_code);
    Logger.info(ctx.id, 'codes', codes, 'messages', messages);
    const lotteResDataList: ILotteModifyOrderNormalResponseData = getElementAtIndex<
      ILotteModifyOrderNormalResponseData
    >(lotteRes.data_list);
    if (codes === null || codes === '0318' || codes === '0307' || codes === '0305') {
      const response: IModifyOrderNormalResponse = {
        orderNumber: lotteResDataList.new_ord_no,
      };
      return response;
    } else {
      throw new GeneralError(`${Constants.ORDER_MODIFY}${codes}`);
    }
  }

  async getHistoryOrder(request: IHistoryOrderRequest, ctx: IContext): Promise<IHistoryOrderResponse[]> {
    const error = new InvalidParameterError();
    validate(request.accountNumber, 'accountNumber')
      .setRequire()
      .add(validateRequestAccountNoCreator(request))
      .throwValid(error);
    error.throwErr();
    const srtType: SortType = request.sortType != null ? SORT_TYPE[request.sortType] : SORT_TYPE.DESC;
    const defaultNextDate = srtType === SORT_TYPE.DESC ? config.defaultNextDateDesc : config.defaultNextDateAsc;
    const defaultNextKey = srtType === SORT_TYPE.DESC ? config.defaultNextKeyDesc : config.defaultNextKeyAscOrder;
    const lotteRequest: ILotteHistoryOrderRequest = {
      acnt_no: setDefault<string>(request.accountNumber, request.headers.token.userData.username).toUpperCase(),
      sub_no: setDefault<string>(request.subNumber, config.defaultSubNumber),
      from_dt: setDefault<string>(request.fromDate, config.defaultFromDate),
      to_dt: setDefault<string>(request.toDate, formatDateToDisplay(new Date(), DATE_DISPLAY_FORMAT)),
      sellbuy_type:
        request.sellBuyType != null
          ? setDefault<string>(SELL_BUY_TYPE_LOTTE[request.sellBuyType], request.sellBuyType)
          : SELL_BUY_TYPE_LOTTE.ALL,
      stock_code: setDefault<string>(request.stockCode, config.defaultStockCode),
      srt_type: srtType,
      mth_type:
        request.matchType != null
          ? setDefault<string>(MATCH_TYPE[request.matchType], request.matchType)
          : MATCH_TYPE.ALL,
      mkt_type:
        request.marketType != null
          ? setDefault<string>(MARKET_TYPE[request.marketType], request.marketType)
          : MARKET_TYPE.ALL,
      next_date: setDefault<string>(request.lastOrderDate, defaultNextDate),
      next_key: setDefault<string>(request.nextKey, defaultNextKey),
      row_count: setDefault<number>(request.fetchCount, config.defaultFetchCount).toString(),
    };
    const lotteRes: ILotteHistoryOrderResponse = await this.lotteOrderDao.getHistoryOrder(lotteRequest, ctx);
    const { codes, messages } = parseMessages(lotteRes.error_desc, lotteRes.error_code);
    Logger.info(ctx.id, 'codes', codes, 'memssages', messages);
    if (codes === null || codes === '0011') {
      return lotteRes.data_list.map((item: ILotteHistoryOrderResponseData) => {
        const response: IHistoryOrderResponse = {
          bankName: item.bnk_nm,
          username: item.usr_id,
          orderDate: item.ord_dt,
          orderTime: item.ord_tm,
          orderType: getKeyByValue(item.ord_tp, ORDER_TYPE),
          stockCode: item.stk_cd,
          subNumber: item.sub_no,
          orderPrice: Number(item.ord_pri),
          orderAmount: Number(item.ord_pri) * Number(item.ord_qty),
          orderNumber: item.ord_no,
          orderStatus: getKeyByValue(item.ord_stat, ORDER_STATUS),
          sellBuyType: getKeyByValue(item.sellbuy_tp, SELL_BUY_TYPE_LOTTE),
          matchedPrice: Number(item.mth_pri),
          accountNumber: item.acnt_no,
          matchedAmount: Number(item.mth_pri) * Number(item.mth_qty),
          orderQuantity: Number(item.ord_qty),
          matchedQuantity: Number(item.mth_qty),
          modifyCancelType: getKeyByValue(item.modcan_tp, ORDER_MODIFY_CANCEL_TYPE),
          unmatchedQuantity: Number(item.unmth_qty),
          originalOrderNumber: item.orgord_no,
          modifyCancelQuantity: Number(item.modcan_qty),
          nextKey: item.next_key,
        };
        return response;
      });
    } else if (codes !== null && codes === '2016') {
      return [];
    } else {
      throw new GeneralError(`${Constants.ORDER_HISTORY}${codes}`);
    }
  }

  async getTodayUnmatchOrder(request: ITodayUnmatchOrderRequest, ctx: IContext) {
    const error = new InvalidParameterError();
    validate(request.accountNumber, 'accountNumber')
      .setRequire()
      .add(validateRequestAccountNoCreator(request))
      .throwValid(error);
    error.throwErr();
    let nextKey: string = config.defaultNextKeyDesc;
    if (request.lastBranchCode != null && request.lastBranchCode.length > 0) {
      nextKey = request.lastBranchCode.padStart(3, '0') + nextKey.slice(3);
    }
    if (request.lastOrderNumber != null && request.lastOrderNumber.length > 0) {
      nextKey = nextKey.slice(0, 3) + request.lastOrderNumber.padStart(7, '0') + nextKey.slice(10);
    }
    if (request.lastOrderPrice != null && request.lastOrderPrice.length > 0) {
      nextKey = nextKey.slice(0, 10) + request.lastOrderPrice.padStart(10, '0');
    }
    const lotteRequest: ILotteTodayUnmatchOrderRequest = {
      acnt_no: setDefault<string>(request.accountNumber, request.headers.token.userData.username).toUpperCase(),
      sub_no: setDefault<string>(request.subNumber, config.defaultSubNumber),
      stock_code: setDefault<string>(request.stockCode, config.defaultStockCode),
      next_key: nextKey,
      row_count: setDefault<number>(request.fetchCount, config.defaultFetchCount).toString(),
    };
    const lotteRes: ILotteTodayUnmatchOrderResponse = await this.lotteOrderDao.getTodayUnmatchOrder(lotteRequest, ctx);
    const { codes, messages } = parseMessages(lotteRes.error_desc, lotteRes.error_code);
    Logger.info(ctx.id, 'codes', codes, 'messages', messages);
    if (codes === null || codes === '0011') {
      return lotteRes.data_list.map((item: ILotteTodayUnmatchOrderResponseData) => {
        const response: ITodayUnmatchOrderResponse = {
          channel: setDefault<string>(CHANNEL_TYPE[item.mdm_tp], item.mdm_tp),
          bankCode: item.bnk_cd,
          bankName: item.bnk_nm,
          username: item.usr_id,
          orderTime: item.ord_tm,
          orderType: getKeyByValue(item.ord_tp, ORDER_TYPE),
          stockCode: item.stk_cd,
          branchCode: item.bnh_cd,
          marketType: setDefault<string>(MARKET_TYPE_LOTTE[item.mkttrd_tp], item.mkttrd_tp),
          orderPrice: Number(item.ord_pri),
          orderNumber: item.ord_no,
          orderStatus: getKeyByValue(item.ord_stat, ORDER_STATUS),
          sellBuyType: getKeyByValue(item.sellbuy_tp, SELL_BUY_TYPE_LOTTE),
          orderQuantity: Number(item.ord_qty),
          unmatchedQuantity: Number(item.unmth_qty),
          originalOrderNumber: item.org_ordno,
        };
        return response;
      });
    } else if (codes !== null && codes === '2016') {
      return [];
    } else {
      throw new GeneralError(`${Constants.ORDER_TODAY_UNMATCH}${codes}`);
    }
  }

  async confirmOrder(request: IOrderConfirmRequest, ctx: IContext): Promise<IOrderConfirmResponse> {
    const ordNo: string = request.orders.reduce((acc, cur) => `${acc}${cur.orderDate}${cur.orderNumber};`, '');
    const lotteRequest: ILotteOrderConfirmRequest = {
      hts_user_id: request.headers.token.userData.username,
      acnt_no: setDefault<string>(request.accountNumber, request.headers.token.userData.username).toUpperCase(),
      sub_no: setDefault<string>(request.subNumber, config.defaultSubNumber),
      ord_strs: ordNo,
    };
    const lotteRes: ILotteOrderConfirmResponse = await this.lotteOrderDao.confirmOrder(lotteRequest, ctx);
    const { codes, messages } = parseMessages(lotteRes.error_desc, lotteRes.error_code);
    Logger.info(ctx.id, 'codes', codes, 'messages', messages);
    if (codes === null || codes === '3366') {
      const response: IOrderConfirmResponse = {};
      return response;
    } else {
      throw new GeneralError(`${Constants.ORDER_CONFIRM}${codes}`);
    }
  }

  async searchOrderConfirm(
    request: IOrderConfirmHistoryRequest,
    ctx: IContext
  ): Promise<IOrderConfirmHistoryResponse[]> {
    const lotteRequest: ILotteOrderConfirmHistoryRequest = {
      acnt_no: setDefault<string>(request.accountNumber, request.headers.token.userData.username).toUpperCase(),
      sub_no: setDefault<string>(request.subNumber, config.defaultSubNumber),
      from_date: setDefault<string>(request.fromDate, config.defaultFromDate),
      to_date: setDefault<string>(request.toDate, formatDateToDisplay(new Date(), DATE_DISPLAY_FORMAT)),
      accp_tp: setDefault<string>(request.confirmStatus, config.defaultConfirmStatus),
      sell_buy_tp: request.sellBuyType != null ? SELL_BUY_TYPE_LOTTE[request.sellBuyType] : SELL_BUY_TYPE_LOTTE.ALL,
      stk_cd: setDefault<string>(request.stockCode, config.defaultStockCode),
      crrt_cncl_tp: setDefault<string>(request.cancelType, config.defaultStockCode),
      mkt_trd_tp: request.marketType != null ? MARKET_TYPE[request.marketType] : MARKET_TYPE.ALL,
      mdm_tp: config.defaultChannelType,
      next_key: setDefault<string>(request.nextKey, config.defaultNextKeyAscOrder),
      row_count: setDefault<number>(request.fetchCount, config.defaultFetchCount).toString(),
    };
    const lotteRes: ILotteOrderConfirmHistoryResponse = await this.lotteOrderDao.searchOrderConfirm(lotteRequest, ctx);
    const { codes, messages } = parseMessages(lotteRes.error_desc, lotteRes.error_code);
    Logger.info(ctx.id, 'codes', codes, 'messages', messages);
    if (codes === null || codes === '0011') {
      return lotteRes.data_list.map((item: ILotteOrderConfirmHistoryResponseData) => {
        const response: IOrderConfirmHistoryResponse = {
          accountNumber: item.acnt_no,
          orderDate: item.stk_ord_dt,
          orderTime: item.ord_time,
          sellBuyType: getKeyByValue(item.sell_buy_tp, SELL_BUY_TYPE_LOTTE),
          orderNumber: item.ord_no,
          stockCode: item.stk_cd,
          orderQuantity: Number(item.ord_qty),
          orderPrice: Number(item.ord_pri),
          matchedQuantity: Number(item.mth_qty),
          matchedPrice: Number(item.mth_pri),
          confirmStatus: item.accp_tp,
          broker: item.work_mn,
          mediaType: getKeyByValue(item.mdm_tp, CHANNEL_TYPE),
          orderType: getKeyByValue(item.stk_ord_tp, ORDER_TYPE),
          nextKey: item.next_key,
        };
        return response;
      });
    } else if (codes !== null && codes === '2016') {
      return [];
    } else {
      throw new GeneralError(`${Constants.ORDER_CONFIRM_SEARCH}${codes}`);
    }
  }

  async enterAdvanceOrder(request: IAdvancedOrderRequest, ctx: IContext): Promise<IAdvancedOrderResponse> {
    const grantType: string = request.headers?.token?.grantType;
    const error = new InvalidParameterError();
    validate(request.accountNumber, 'accountNumber')
      .setRequire()
      .add(validateRequestAccountNoCreator(request))
      .throwValid(error);
    validate(request.subNumber, 'subNumber')
      .add(validateSubAccount(request, false))
      .throwValid(error);
    validate(request.stockCode, 'stockCode')
      .setRequire()
      .throwValid(error);
    validate(request.orderQuantity, 'orderQuantity')
      .setRequire()
      .throwValid(error);
    validate(request.sellBuyType, 'sellBuyType')
      .setRequire()
      .throwValid(error);
    validate(request.orderType, 'orderType')
      .setRequire()
      .throwValid(error);
    validate(request.phoneNumber, 'phoneNumber')
      .setRequire()
      .throwValid(error);
    validate(request.deviceUniqueId, 'deviceUniqueId')
      .setRequire()
      .throwValid(error);
    if (request.orderType === ORDER_TYPE.LO) {
      validate(request.orderPrice, 'orderPrice')
        .setRequire()
        .throwValid(error);
    }
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
    let platform: string = setDefault<string | null | undefined>(request.channel, request.headers.token.platform);
    if (Utils.isEmpty(platform) && grantType === 'client_credentials') {
      platform = config.platformDifiSoft;
    }
    const lotteRequest: ILotteAdvancedOrderRequest = {
      bank_code: bankCode,
      stk_ord_tp: ORDER_TYPE[request.orderType],
      stk_cd: request.stockCode,
      sub_no: subNumber,
      ord_pri: request.orderPrice,
      bank_acnt: request.bankAccount,
      phone_num: request.phoneNumber,
      acnt_no: request.accountNumber.toUpperCase(),
      ord_qty: request.orderQuantity,
      hts_user_id: request.headers.token.userData.username,
      hts_user_nm: request.headers.token.userData['name'],
      idno: request.headers.token.userData.identifierNumber,
      cli_mac_addr: request.deviceUniqueId,
      cli_ip_addr: request.sourceIp,
      mdm_tp: getPlatformValueCore(platform),
    };
    let lotteRes: ILotteAdvancedOrderResponse;
    switch (request.sellBuyType) {
      case SELL_BUY_TYPE.BUY:
        lotteRes = await this.lotteOrderDao.enterBuyAdvancedOrder(lotteRequest, ctx);
        break;
      case SELL_BUY_TYPE.SELL:
        lotteRes = await this.lotteOrderDao.enterSellAdvancedOrder(lotteRequest, ctx);
        break;
      default:
        throw new InvalidParameterError().add('INVALID_VALUE', 'sellBuyType', [request.sellBuyType]);
    }
    const { codes, messages } = parseMessages(lotteRes.error_desc, lotteRes.error_code);
    Logger.info(ctx.id, 'codes', codes, 'messages', messages);
    const lotteResDataList: ILotteAdvancedOrderResponseData = getElementAtIndex<ILotteAdvancedOrderResponseData>(
      lotteRes.data_list
    );
    if (codes === null || codes === '0350') {
      const response: IAdvancedOrderResponse = {
        message: messages,
        tempOrderNumber: lotteResDataList.new_ord_no,
      };
      return response;
    } else {
      throw new GeneralError(`${Constants.ORDER_ADVANCE_PLACE}${codes}`);
    }
  }

  async cancelAdvanceOrder(request: ICancelOrderAdvancedRequest, ctx: IContext): Promise<ICancelOrderAdvancedResponse> {
    const error = new InvalidParameterError();
    validate(request.accountNumber, 'accountNumber')
      .setRequire()
      .add(validateRequestAccountNoCreator(request))
      .throwValid(error);
    validate(request.orderNumber, 'orderNumber')
      .setRequire()
      .throwValid(error);
    validate(request.advanceOrderDate, 'advanceOrderDate')
      .setRequire()
      .throwValid(error);
    error.throwErr();
    const grantType: string = request.headers?.token?.grantType;
    let platform: string = setDefault<string | null | undefined>(request.channel, request.headers.token.platform);
    if (Utils.isEmpty(platform) && grantType === 'client_credentials') {
      platform = config.platformDifiSoft;
    }
    const lotteRequest: ILotteCancelOrderAdvancedRequest = {
      acnt_no: request.accountNumber.toUpperCase(),
      sub_no: setDefault<string>(request.subNumber, config.defaultSubNumber),
      ord_no: request.orderNumber,
      ord_frt_dt: request.advanceOrderDate,
      hts_user_id: request.headers.token.userData.username,
      hts_user_nm: request.headers.token.userData['name'],
      cli_ip_addr: request.sourceIp,
      cli_mac_addr: request.deviceUniqueId,
      mdm_tp: getPlatformValueCore(platform),
    };
    const lotteRes: ILotteCancelOrderAdvancedResponse = await this.lotteOrderDao.cancelAdvancedOrder(lotteRequest, ctx);
    const { codes, messages } = parseMessages(lotteRes.error_desc, lotteRes.error_code);
    const lotteResDataList: ILotteCancelAdvancedOrderResponseData = getElementAtIndex<
      ILotteCancelAdvancedOrderResponseData
    >(lotteRes.data_list);
    Logger.info(ctx.id, 'codes', codes, 'messages', messages);
    if (codes === null || codes === '0320') {
      const response: ICancelOrderAdvancedResponse = {
        orderNumber: lotteResDataList.ord_no,
      };
      return response;
    } else {
      throw new GeneralError(`${Constants.ORDER_ADVANCE_CANCEL}${codes}`);
    }
  }

  async getHistoryOrderAdvanced(
    request: IHistoryOrderAdvancedRequest,
    ctx: IContext
  ): Promise<IHistoryOrderAdvancedResponse[]> {
    const error = new InvalidParameterError();
    validate(request.accountNumber, 'accountNumber')
      .setRequire()
      .add(validateRequestAccountNoCreator(request))
      .throwValid(error);
    error.throwErr();
    const nextKey: string =
      setDefault<string>(request.lastOrderDate, '').padStart(8, '9') +
      setDefault<string>(request.lastOrderNumber, '').padStart(6, '0');
    const lotteRequest: ILotteHistoryOrderAdvancedRequest = {
      acnt_no: setDefault<string>(request.accountNumber, request.headers.token.userData.username).toUpperCase(),
      sub_no: setDefault<string>(request.subNumber, config.defaultSubNumber),
      stk_cd: setDefault<string>(request.stockCode, config.defaultStockCode),
      mkt_tp:
        request.marketType != null
          ? setDefault<string>(MARKET_TYPE_ADVANCED[request.marketType], request.marketType)
          : MARKET_TYPE_ADVANCED.ALL,
      sell_buy_tp:
        request.sellBuyType != null
          ? setDefault<string>(SELL_BUY_TYPE_ADVANCED[request.sellBuyType], request.marketType)
          : SELL_BUY_TYPE_ADVANCED.ALL,
      next_key: nextKey,
      row_count: setDefault<number>(request.fetchCount, config.defaultFetchCount).toString(),
    };
    const lotteRes: ILotteHistoryOrderAdvancedResponse = await this.lotteOrderDao.getHistoryOrderAdvanced(
      lotteRequest,
      ctx
    );
    const { codes, messages } = parseMessages(lotteRes.error_desc, lotteRes.error_code);
    Logger.info(ctx.id, 'codes', codes, 'messages', messages);
    if (codes === null || codes === '0011') {
      return lotteRes.data_list.map((item) => {
        const response: IHistoryOrderAdvancedResponse = {
          orderDate: item.ord_frct_dt,
          orderTime: item.ord_time,
          orderNumber: item.ord_no,
          stockCode: item.stk_cd,
          sellBuyType: getKeyByValue(item.sell_buy_tp, SELL_BUY_TYPE_ADVANCED),
          orderType: getKeyByValue(item.stk_ord_tp, ORDER_TYPE),
          orderPrice: Number(item.ord_pri),
          orderQuantity: Number(item.ord_qty),
          username: item.work_nm,
          channel: setDefault<string>(CHANNEL_TYPE[item.mdm_tp], item.mdm_tp),
          orderStatus: item.accp_tp,
        };
        return response;
      });
    } else if (codes !== null && codes === '2016') {
      return [];
    } else {
      throw new GeneralError(`${Constants.ORDER_ADVANCE_HISTORY}${codes}`);
    }
  }

  async getOrderBook(request: IOrderBookRequest, ctx: IContext): Promise<IOrderBookResponse[]> {
    const error = new InvalidParameterError();
    validate(request.accountNumber, 'accountNumber')
      .setRequire()
      .add(validateRequestAccountNoCreator(request))
      .throwValid(error);
    validate(request.subNumber, 'subNumber')
      .setRequire()
      .throwValid(error);
    error.throwErr();
    const lotteRequest: ILotteOrderBookRequest = {
      acnt_no: request.accountNumber.toUpperCase(),
      sub_no: request.subNumber,
      sellbuy_type:
        request.sellBuyType != null
          ? setDefault<string>(SELL_BUY_TYPE_LOTTE[request.sellBuyType], request.sellBuyType)
          : SELL_BUY_TYPE_LOTTE.ALL,
      stock_code: setDefault<string>(checkStringTrim(request.stockCode), config.defaultStockCode),
      mth_type:
        request.matchType != null
          ? setDefault<string>(MATCH_TYPE[request.matchType], request.matchType)
          : MATCH_TYPE.ALL,
      next_date: setDefault<string>(checkStringTrim(request.lastOrderDate), config.defaultNextDateDesc),
      next_key: setDefault<string>(checkStringTrim(request.nextKey), config.defaultNextKeyDesc),
      row_count: setDefault<number>(request.fetchCount, config.defaultFetchCount).toString(),
    };
    const lotteRes: ILotteOrderBookResponse = await this.lotteOrderDao.getOrderBook(lotteRequest, ctx);
    const { codes } = parseMessages(lotteRes.error_desc, lotteRes.error_code);
    if (codes === null) {
      return lotteRes.data_list.map(
        (item: ILotteOrderBookResponseData): IOrderBookResponse => {
          const response: IOrderBookResponse = {
            orderTime: item.ord_tm,
            stockCode: item.stk_cd,
            sellBuyType: getKeyByValue(item.sellbuy_tp, SELL_BUY_TYPE_LOTTE),
            orderType: getKeyByValue(item.ord_tp, ORDER_TYPE),
            orderQuantity: Number(item.ord_qty),
            orderPrice: Number(item.ord_pri),
            matchedQuantity: Number(item.mth_qty),
            matchedPrice: Number(item.mth_pri),
            unmatchedQuantity: Number(item.unmth_qty),
            modifyCancelType: getKeyByValue(item.modcan_tp, ORDER_MODIFY_CANCEL_TYPE),
            orderStatus: getKeyByValue(item.ord_stat, ORDER_STATUS),
            orderNumber: Number(item.ord_no),
            originalOrderNumber: Number(item.orgord_no),
            canModifyCancel: item.mod_stt === 'Y',
            orderDate: item.next_date,
            nextKey: item.next_key,
          };
          return response;
        }
      );
    } else if (codes !== null && codes === '2016') {
      return [];
    } else {
      throw new GeneralError(lotteRes.error_desc);
    }
  }
}
