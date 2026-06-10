import { Inject } from 'typedi';
import { IContext } from '../../models/IContext';
import {
  ILotteRightHistoryBaseItem,
  IRightHistoryBaseItem,
  IRightHistoryBaseRequest,
  IRightHistoryBaseResponse,
} from '../../models/right-history/base';
import { ILotteRightHistoryBaseRequest, ILotteRightHistoryBaseResponse } from '../../models/right-history/base';
import { parseMessages, setDefault, validateRequestAccountNoCreator } from '../../utils/lotte';
import { GeneralError, InvalidParameterError } from 'tradex-common/build/src/modules/errors';
import config from '../../config';
import { IRightHistoryStrategy } from './IRightHistoryStrategy';
import { LotteBalanceDao } from '../../daos/LotteBalanceDao';
import { Utils } from 'tradex-common';

const { validate } = Utils;

export abstract class AbstractRightHistoryService implements IRightHistoryStrategy {
  @Inject()
  protected lotteBalanceDao: LotteBalanceDao;

  async processRightHistory(
    request: IRightHistoryBaseRequest,
    ctx: IContext
  ): Promise<IRightHistoryBaseResponse<ILotteRightHistoryBaseItem>> {
    // Step 1: Required inputs validation (BR-07)
    this.validateRequiredInputs(request);

    // Step 2: Build Lotte request (BR-01, BR-02, BR-03)
    const lotteRequest = this.buildLotteRequest(request);

    // Step 3: Call Lotte API (DIFFERENT - delegated to strategy)
    const lotteResponse = await this.callLotteAPI(lotteRequest, ctx);

    // Step 4: Handle business errors (BR-08)
    this.handleBusinessErrors(lotteResponse);

    // Step 5: Transform response (DIFFERENT - delegated to strategy) (BR-04, BR-05, BR-06)
    return this.transformResponse(lotteResponse);
  }

  // BR-07: Required inputs validation
  protected validateRequiredInputs(request: IRightHistoryBaseRequest): void {
    const error = new InvalidParameterError();
    validate(request.accountNo, 'accountNo')
      .setRequire()
      .add(validateRequestAccountNoCreator(request))
      .throwValid(error);
    validate(request.subNo, 'subNo')
      .setRequire()
      .throwValid(error);
    error.throwErr();
  }

  // BR-01, BR-02, BR-03: Build request with defaults and pagination
  protected buildLotteRequest(request: IRightHistoryBaseRequest): ILotteRightHistoryBaseRequest {
    // BR-01: Apply defaults
    const symbol = setDefault<string>(request.symbol, config.defaultStockCode);
    const langCode = setDefault<string>(request.headers?.['accept-language'], config.defaultLanguage);
    const fetchCount = setDefault<number>(request.fetchCount, config.defaultFetchCount);

    // BR-02, BR-03: Handle pagination
    const nextKey = request.nextKey && request.nextKey !== '' ? request.nextKey : '0';

    return {
      acnt_no: request.accountNo.toUpperCase(),
      sub_no: request.subNo,
      stk_cd: symbol,
      mkt_tp: request.marketType,
      next_key: nextKey,
      row_count: fetchCount,
      lang_code: langCode,
    };
  }

  // BR-08: Handle Core business errors
  protected handleBusinessErrors(lotteResponse: ILotteRightHistoryBaseResponse<ILotteRightHistoryBaseItem>): void {
    const { codes } = parseMessages(lotteResponse.error_desc, lotteResponse.error_code);

    // BR-08: Pass-through Core errors (except success cases)
    if (codes !== null && codes !== '2016') {
      throw new GeneralError(lotteResponse.error_desc);
    }
  }

  // Abstract methods - DIFFERENT parts delegated to strategies
  protected abstract callLotteAPI(
    request: ILotteRightHistoryBaseRequest,
    ctx: IContext
  ): Promise<ILotteRightHistoryBaseResponse<ILotteRightHistoryBaseItem>>;

  protected abstract transformResponse(
    lotteResponse: ILotteRightHistoryBaseResponse<ILotteRightHistoryBaseItem>
  ): IRightHistoryBaseResponse<IRightHistoryBaseItem>;

  // BR-05: Utility method for numeric conversion
  protected toNumberIfPossible(value: string | null | undefined): number | null {
    if (!value || value.trim() === '') return null;
    const num = Number(value);
    return isNaN(num) ? null : num;
  }

  protected mapYNToBoolean(value: string): boolean | null {
    if (value === 'Y') return true;
    if (value === 'N') return false;
    return null;
  }
}
