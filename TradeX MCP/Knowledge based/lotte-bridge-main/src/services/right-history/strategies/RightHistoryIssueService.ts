import { Service } from 'typedi';
import { AbstractRightHistoryService } from '../AbstractRightHistoryService';
import {
  ILotteRightHistoryIssueItem,
  ILotteRightHistoryIssueRequest,
  ILotteRightHistoryIssueResponse,
  IRightHistoryIssueResponse,
} from '../../../models/right-history/issue';
import { IContext } from '../../../models/IContext';
import { ILotteRightHistoryDataList } from '../../../models/right-history/base/ILotteRightHistoryBaseResponse';
import { getElementAtIndex } from '../../../utils/lotte';

@Service()
export class RightHistoryIssueService extends AbstractRightHistoryService {
  protected async callLotteAPI(
    request: ILotteRightHistoryIssueRequest,
    ctx: IContext
  ): Promise<ILotteRightHistoryIssueResponse> {
    // Call specific Lotte API endpoint for "Issue" right history
    return this.lotteBalanceDao.rightHistoryIssue(request, ctx);
  }

  protected transformResponse(lotteResponse: ILotteRightHistoryIssueResponse): IRightHistoryIssueResponse {
    const data: ILotteRightHistoryDataList<ILotteRightHistoryIssueItem> = getElementAtIndex<
      ILotteRightHistoryDataList<ILotteRightHistoryIssueItem>
    >(lotteResponse.data_list);

    // BR-06: Handle no-data success case
    if (!data || !data.listItems) {
      return {} as IRightHistoryIssueResponse;
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
        beginDate: item.begin_date,
        endDate: item.end_date,
        issuePrice: this.toNumberIfPossible(item.issue_price),
        availableQuantity: this.toNumberIfPossible(item.avail_qtty),
        requestedQuantity: this.toNumberIfPossible(item.req_qtty),
        requestedAmount: this.toNumberIfPossible(item.req_amt),
        effectDate: item.effect_date,
        isEffective: this.mapYNToBoolean(item.effect_yn),
      })),
      nextKey: data.next_data || '',
    };
  }
}
