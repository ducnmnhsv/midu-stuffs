import { Service } from 'typedi';
import { AbstractRightHistoryService } from '../AbstractRightHistoryService';
import {
  ILotteRightHistoryDividendItem,
  ILotteRightHistoryDividendRequest,
  ILotteRightHistoryDividendResponse,
  IRightHistoryDividendResponse,
} from '../../../models/right-history/dividend';
import { IContext } from '../../../models/IContext';
import { ILotteRightHistoryDataList } from '../../../models/right-history/base/ILotteRightHistoryBaseResponse';
import { getElementAtIndex } from '../../../utils/lotte';

@Service()
export class RightHistoryDividendService extends AbstractRightHistoryService {
  protected async callLotteAPI(
    request: ILotteRightHistoryDividendRequest,
    ctx: IContext
  ): Promise<ILotteRightHistoryDividendResponse> {
    // Call specific Lotte API endpoint for "Dividend" right history
    return this.lotteBalanceDao.rightHistoryDividend(request, ctx);
  }

  protected transformResponse(lotteResponse: ILotteRightHistoryDividendResponse): IRightHistoryDividendResponse {
    const data: ILotteRightHistoryDataList<ILotteRightHistoryDividendItem> = getElementAtIndex<
      ILotteRightHistoryDataList<ILotteRightHistoryDividendItem>
    >(lotteResponse.data_list);

    // BR-06: Handle no-data success case
    if (!data || !data.listItems) {
      return {} as IRightHistoryDividendResponse;
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
        ownedQty: this.toNumberIfPossible(item.own_qtty),
        availableDividendQty: this.toNumberIfPossible(item.avail_qtty),
        effectDate: item.effect_date,
        isEffective: this.mapYNToBoolean(item.effect_yn),
        taxRate: this.toNumberIfPossible(item.tax_rate),
        cashDivAmount: this.toNumberIfPossible(item.asn_amt),
        dividendPayDate: item.divi_pay_dt,
        isCashDivReceive: this.mapYNToBoolean(item.rcpt_trd_no_yn),
        oddLotPrice: this.toNumberIfPossible(item.flotq_std_pri),
        oddLotAmount: this.toNumberIfPossible(item.flotq_amt),
        oddLotPayDate: item.flotq_pay_dt,
        isOddLotPaid: this.mapYNToBoolean(item.rgt_proc_stat_yn),
      })),
      nextKey: data.next_data || '',
    };
  }
}
