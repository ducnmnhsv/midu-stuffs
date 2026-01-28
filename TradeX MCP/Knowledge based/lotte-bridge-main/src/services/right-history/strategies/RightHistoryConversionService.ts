import { Service } from 'typedi';
import { AbstractRightHistoryService } from '../AbstractRightHistoryService';
import {
  ILotteRightHistoryConversionItem,
  ILotteRightHistoryConversionRequest,
  ILotteRightHistoryConversionResponse,
  IRightHistoryConversionResponse,
} from '../../../models/right-history/conversion';
import { IContext } from '../../../models/IContext';
import { ILotteRightHistoryDataList } from '../../../models/right-history/base/ILotteRightHistoryBaseResponse';
import { getElementAtIndex } from '../../../utils/lotte';

@Service()
export class RightHistoryConversionService extends AbstractRightHistoryService {
  protected async callLotteAPI(
    request: ILotteRightHistoryConversionRequest,
    ctx: IContext
  ): Promise<ILotteRightHistoryConversionResponse> {
    // Call specific Lotte API endpoint for "Conversion" right history
    return this.lotteBalanceDao.rightHistoryConversion(request, ctx);
  }

  protected transformResponse(lotteResponse: ILotteRightHistoryConversionResponse): IRightHistoryConversionResponse {
    const data: ILotteRightHistoryDataList<ILotteRightHistoryConversionItem> = getElementAtIndex<
      ILotteRightHistoryDataList<ILotteRightHistoryConversionItem>
    >(lotteResponse.data_list);

    // BR-06: Handle no-data success case
    if (!data || !data.listItems) {
      return {} as IRightHistoryConversionResponse;
    }

    return {
      // Transform items with BR-05: Numeric normalization
      items: data.listItems.map((item) => ({
        symbol: item.symbol,
        sequence: this.toNumberIfPossible(item.seq),
        baseDate: item.base_date,
        status: item.status,
        baseRate: this.toNumberIfPossible(item.base_rate),
        conversionRate: this.toNumberIfPossible(item.cnvt_rate),
        ownedQuantity: this.toNumberIfPossible(item.own_qtty),
        convertedSymbol: item.cnvt_stk_cd,
        convertedQuantity: this.toNumberIfPossible(item.avail_qtty),
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
