import { Service } from 'typedi';
import { RightHistoryType } from './types/RightHistoryType';
import { RightHistoryStrategyFactory } from './factories/RightHistoryStrategyFactory';
import { IContext } from '../../models/IContext';
import {
  IRightHistoryBaseItem,
  IRightHistoryBaseRequest,
  IRightHistoryBaseResponse,
} from '../../models/right-history/base';

@Service()
export class RightHistoryService {
  async getRightHistory(
    request: IRightHistoryBaseRequest,
    rightType: RightHistoryType,
    ctx: IContext
  ): Promise<IRightHistoryBaseResponse<IRightHistoryBaseItem>> {
    const strategy = RightHistoryStrategyFactory.getStrategy(rightType);
    return strategy.processRightHistory(request, ctx);
  }
}
