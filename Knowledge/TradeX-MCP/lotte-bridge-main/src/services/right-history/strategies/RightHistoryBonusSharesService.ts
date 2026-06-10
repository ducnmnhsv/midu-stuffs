import { Service } from 'typedi';
import { AbstractRightHistoryService } from '../AbstractRightHistoryService';
import {
  ILotteRightHistoryBonusSharesItem,
  ILotteRightHistoryBonusSharesRequest,
  ILotteRightHistoryBonusSharesResponse,
  IRightHistoryBonusSharesResponse,
} from '../../../models/right-history/bonus-shares';
import { IContext } from '../../../models/IContext';
import { ILotteRightHistoryDataList } from '../../../models/right-history/base/ILotteRightHistoryBaseResponse';
import { getElementAtIndex } from '../../../utils/lotte';

@Service()
export class RightHistoryBonusSharesService extends AbstractRightHistoryService {
  protected async callLotteAPI(
    request: ILotteRightHistoryBonusSharesRequest,
    ctx: IContext
  ): Promise<ILotteRightHistoryBonusSharesResponse> {
    // Call specific Lotte API endpoint for "BonusShares" right history
    return this.lotteBalanceDao.rightHistoryBonusShares(request, ctx);
  }

  protected transformResponse(lotteResponse: ILotteRightHistoryBonusSharesResponse): IRightHistoryBonusSharesResponse {
    const data: ILotteRightHistoryDataList<ILotteRightHistoryBonusSharesItem> = getElementAtIndex<
      ILotteRightHistoryDataList<ILotteRightHistoryBonusSharesItem>
    >(lotteResponse.data_list);

    // BR-06: Handle no-data success case
    if (!data || !data.listItems) {
      return {} as IRightHistoryBonusSharesResponse;
    }

    return {
      // Transform items with BR-05: Numeric normalization
      items: data.listItems.map((item) => ({
        symbol: item.symbol,
        sequence: this.toNumberIfPossible(item.seq),
        baseDate: item.base_date,
        status: item.status,
        baseRate: this.toNumberIfPossible(item.base_rate),
        dividendRate: this.toNumberIfPossible(item.divd_rate),
        ownedQuantity: this.toNumberIfPossible(item.own_qtty),
        availableQuantity: this.toNumberIfPossible(item.avail_qtty),
        effectDate: item.effect_date,
        isEffective: this.mapYNToBoolean(item.effect_yn),
        oddLotPrice: this.toNumberIfPossible(item.flotq_std_pri),
        oddLotAmount: this.toNumberIfPossible(item.flotq_amt),
        oddLotPayDate: item.flotq_pay_dt,
        isOddLotPaid: this.mapYNToBoolean(item.flotq_stat_yn),
      })),
      nextKey: data.next_data || '',
    };
  }
}
