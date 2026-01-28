import { Service } from 'typedi';
import { AbstractRightHistoryService } from '../AbstractRightHistoryService';
import {
  ILotteRightHistoryBondInterestItem,
  ILotteRightHistoryBondInterestRequest,
  ILotteRightHistoryBondInterestResponse,
  IRightHistoryBondInterestResponse,
} from '../../../models/right-history/bond-interest';
import { IContext } from '../../../models/IContext';
import { ILotteRightHistoryDataList } from '../../../models/right-history/base/ILotteRightHistoryBaseResponse';
import { getElementAtIndex } from '../../../utils/lotte';

@Service()
export class RightHistoryBondInterestService extends AbstractRightHistoryService {
  protected async callLotteAPI(
    request: ILotteRightHistoryBondInterestRequest,
    ctx: IContext
  ): Promise<ILotteRightHistoryBondInterestResponse> {
    // Call specific Lotte API endpoint for "BondInterest" right history
    return this.lotteBalanceDao.rightHistoryBondInterest(request, ctx);
  }

  protected transformResponse(
    lotteResponse: ILotteRightHistoryBondInterestResponse
  ): IRightHistoryBondInterestResponse {
    const data: ILotteRightHistoryDataList<ILotteRightHistoryBondInterestItem> = getElementAtIndex<
      ILotteRightHistoryDataList<ILotteRightHistoryBondInterestItem>
    >(lotteResponse.data_list);

    // BR-06: Handle no-data success case
    if (!data || !data.listItems) {
      return {} as IRightHistoryBondInterestResponse;
    }

    return {
      // Transform items with BR-05: Numeric normalization
      items: data.listItems.map((item) => ({
        symbol: item.symbol,
        sequence: this.toNumberIfPossible(item.seq),
        baseDate: item.base_date,
        status: item.status,
        distributionRate: this.toNumberIfPossible(item.divd_rate),
        taxRate: this.toNumberIfPossible(item.tax_rate),
        ownedQuantity: this.toNumberIfPossible(item.own_qtty),
        basePrice: this.toNumberIfPossible(item.issue_price),
        principalAmount: this.toNumberIfPossible(item.asn_amt),
        principalPayDate: item.list_dt,
        isPrincipalReceived: this.mapYNToBoolean(item.rcpt_trd_no_yn),
        interestAmount: this.toNumberIfPossible(item.inter_amt),
        interestPayDate: item.inter_dt,
        isInterestPaid: this.mapYNToBoolean(item.effect_yn),
      })),
      nextKey: data.next_data || '',
    };
  }
}
