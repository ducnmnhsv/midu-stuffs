import { Service } from 'typedi';
import { AbstractRightHistoryService } from '../AbstractRightHistoryService';
import { IContext } from '../../../models/IContext';
import {
  ILotteRightHistoryOtherItem,
  ILotteRightHistoryOtherRequest,
  ILotteRightHistoryOtherResponse,
  IRightHistoryOtherResponse,
} from '../../../models/right-history/other';
import { ILotteRightHistoryDataList } from '../../../models/right-history/base/ILotteRightHistoryBaseResponse';
import { getElementAtIndex } from '../../../utils/lotte';

@Service()
export class RightHistoryOtherService extends AbstractRightHistoryService {
  protected async callLotteAPI(
    request: ILotteRightHistoryOtherRequest,
    ctx: IContext
  ): Promise<ILotteRightHistoryOtherResponse> {
    // Call specific Lotte API endpoint for "other" right history
    return this.lotteBalanceDao.rightHistoryOther(request, ctx);
  }

  protected transformResponse(lotteResponse: ILotteRightHistoryOtherResponse): IRightHistoryOtherResponse {
    const data: ILotteRightHistoryDataList<ILotteRightHistoryOtherItem> = getElementAtIndex<
      ILotteRightHistoryDataList<ILotteRightHistoryOtherItem>
    >(lotteResponse.data_list);

    // BR-06: Handle no-data success case
    if (!data || !data.listItems) {
      return {} as IRightHistoryOtherResponse;
    }

    return {
      // Transform items with BR-05: Numeric normalization
      items: data.listItems.map((item) => ({
        symbol: item.symbol,
        sequence: this.toNumberIfPossible(item.seq),
        baseDate: item.base_date,
      })),
      nextKey: data.next_data || '',
    };
  }
}
